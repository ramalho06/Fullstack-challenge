package com.media4all.tracking.external.agent;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.ExternalApiRetryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ExternalAgentClient implements ExternalAgentGateway {

    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 100;

    private final WebClient webClient;
    private final ExternalApiRetryPolicy retryPolicy;

    public ExternalAgentClient(
            @Qualifier("externalApiWebClient") WebClient webClient,
            ExternalApiRetryPolicy retryPolicy
    ) {
        this.webClient = webClient;
        this.retryPolicy = retryPolicy;
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

            String fingerprint = pageFingerprint(pageData);

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
                if (!retryPolicy.canRetry(exception, attempt)) {
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
            return new ExternalApiException(message, statusCode, body, parseRetryAfter(retryAfter));
        }

        return new ExternalApiException(message, statusCode, body);
    }

    private void sleepBeforeRetry(ExternalApiException exception, int attempt) {
        long millis = retryPolicy.delayFor(exception, attempt).toMillis();

        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Interrupted while waiting to retry external API request",
                    exception.getHttpStatus(), exception.getResponseBody());
        }
    }

    private Duration parseRetryAfter(String retryAfter) {
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }

        try {
            long seconds = Long.parseLong(retryAfter.trim());
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            try {
                ZonedDateTime retryAt = ZonedDateTime.parse(retryAfter);
                Duration delay = Duration.between(ZonedDateTime.now(), retryAt);
                return delay.isNegative() ? Duration.ZERO : delay;
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private String pageFingerprint(List<ExternalAgentDto> pageData) {
        return pageData.stream()
                .map(ExternalAgentDto::id)
                .reduce("", (left, right) -> left + "|" + right);
    }
}
