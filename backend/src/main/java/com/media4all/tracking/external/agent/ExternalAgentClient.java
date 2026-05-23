package com.media4all.tracking.external.agent;

import com.media4all.tracking.external.ExternalApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ExternalAgentClient {

    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 100;
    private static final int MAX_RETRIES = 3;

    private final WebClient webClient;

    public ExternalAgentClient(@Qualifier("externalApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<ExternalAgentDto> fetchAllAgents() {
        List<ExternalAgentDto> agents = new ArrayList<>();
        Set<String> seenPageFingerprints = new HashSet<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            ExternalAgentPageResponse response = fetchAgentsPage(page, PAGE_SIZE);
            List<ExternalAgentDto> pageData = response.data() == null ? List.of() : response.data();

            if (pageData.isEmpty()) {
                break;
            }

            String fingerprint = pageData.stream()
                    .map(ExternalAgentDto::id)
                    .reduce("", (left, right) -> left + "|" + right);

            // A API real retorna { data: [...] } e pode não expor metadados claros
            // de paginação. Se uma página repetir a anterior, paramos para evitar loop.
            if (!seenPageFingerprints.add(fingerprint)) {
                break;
            }

            agents.addAll(pageData);

            if (pageData.size() < PAGE_SIZE) {
                break;
            }
        }

        return agents;
    }

    ExternalAgentPageResponse fetchAgentsPage(int page, int size) {
        int attempt = 1;

        while (true) {
            try {
                return webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/agents")
                                .queryParam("page", page)
                                .queryParam("pageSize", size)
                                .build())
                        .exchangeToMono(response -> {
                            HttpStatusCode status = response.statusCode();

                            if (status.is2xxSuccessful()) {
                                return response.bodyToMono(ExternalAgentPageResponse.class);
                            }

                            return response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(toException(status, response.headers().asHttpHeaders(), body)));
                        })
                        .block(Duration.ofSeconds(30));
            } catch (ExternalApiException exception) {
                if (!shouldRetry(exception) || attempt >= MAX_RETRIES) {
                    throw exception;
                }

                sleepBeforeRetry(exception, attempt);
                attempt++;
            }
        }
    }

    private ExternalApiException toException(HttpStatusCode status, HttpHeaders headers, String body) {
        int statusCode = status.value();
        String message = "External API returned HTTP " + statusCode;

        if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
            String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
            return new ExternalApiException(message + retryAfterMessage(retryAfter), statusCode, body);
        }

        return new ExternalApiException(message, statusCode, body);
    }

    private boolean shouldRetry(ExternalApiException exception) {
        Integer status = exception.getHttpStatus();
        return status != null
                && (status == HttpStatus.TOO_MANY_REQUESTS.value()
                || status == HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    private void sleepBeforeRetry(ExternalApiException exception, int attempt) {
        long millis = retryDelay(exception, attempt).toMillis();

        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Interrupted while waiting to retry external API request",
                    exception.getHttpStatus(), exception.getResponseBody());
        }
    }

    private Duration retryDelay(ExternalApiException exception, int attempt) {
        if (Integer.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()).equals(exception.getHttpStatus())) {
            Duration retryAfter = parseRetryAfter(exception.getMessage());
            if (retryAfter != null) {
                return retryAfter;
            }
        }

        long baseMillis = (long) Math.pow(2, attempt - 1) * 1_000L;
        long jitterMillis = ThreadLocalRandom.current().nextLong(100L, 500L);
        return Duration.ofMillis(baseMillis + jitterMillis);
    }

    private Duration parseRetryAfter(String message) {
        String marker = "Retry-After=";
        int start = message.indexOf(marker);

        if (start < 0) {
            return null;
        }

        try {
            long seconds = Long.parseLong(message.substring(start + marker.length()).trim());
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String retryAfterMessage(String retryAfter) {
        return retryAfter == null || retryAfter.isBlank() ? "" : " Retry-After=" + retryAfter;
    }
}
