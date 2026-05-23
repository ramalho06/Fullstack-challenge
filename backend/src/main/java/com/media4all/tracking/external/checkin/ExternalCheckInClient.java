package com.media4all.tracking.external.checkin;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.ExternalApiRetryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class ExternalCheckInClient implements ExternalCheckInGateway {

    private final WebClient webClient;
    private final ExternalApiRetryPolicy retryPolicy;

    public ExternalCheckInClient(
            @Qualifier("externalApiWebClient") WebClient webClient,
            ExternalApiRetryPolicy retryPolicy
    ) {
        this.webClient = webClient;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public List<ExternalCheckInDto> fetchAllCheckIns(String syncToken) {
        int attempt = 1;

        while (true) {
            try {
                ExternalCheckInResponse response = webClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder.path("/api/v1/check-ins");

                            if (StringUtils.hasText(syncToken)) {
                                builder.queryParam("syncToken", syncToken);
                            }

                            return builder.build();
                        })
                        .exchangeToMono(clientResponse -> {
                            HttpStatusCode status = clientResponse.statusCode();

                            if (status.is2xxSuccessful()) {
                                return clientResponse.bodyToMono(ExternalCheckInResponse.class);
                            }

                            return clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(toException(
                                            status,
                                            clientResponse.headers().asHttpHeaders(),
                                            body
                                    )));
                        })
                        .block(Duration.ofSeconds(30));

                return response == null || response.data() == null ? List.of() : response.data();
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
            return new ExternalApiException(
                    message,
                    statusCode,
                    body,
                    parseRetryAfter(headers.getFirst(HttpHeaders.RETRY_AFTER))
            );
        }

        return new ExternalApiException(message, statusCode, body);
    }

    private void sleepBeforeRetry(ExternalApiException exception, int attempt) {
        long millis = retryPolicy.delayFor(exception, attempt).toMillis();

        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException(
                    "Interrupted while waiting to retry external API request",
                    exception.getHttpStatus(),
                    exception.getResponseBody()
            );
        }
    }

    private Duration parseRetryAfter(String retryAfter) {
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }

        try {
            return Duration.ofSeconds(Long.parseLong(retryAfter.trim()));
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
}
