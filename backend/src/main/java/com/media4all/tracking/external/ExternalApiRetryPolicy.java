package com.media4all.tracking.external;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ExternalApiRetryPolicy {

    private static final int MAX_RETRIES = 3;

    public boolean canRetry(ExternalApiException exception, int attempt) {
        return attempt < MAX_RETRIES && shouldRetry(exception);
    }

    public Duration delayFor(ExternalApiException exception, int attempt) {
        if (Integer.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()).equals(exception.getHttpStatus())
                && exception.getRetryAfter() != null) {
            return exception.getRetryAfter();
        }

        long baseMillis = (long) Math.pow(2, attempt - 1) * 1_000L;
        long jitterMillis = ThreadLocalRandom.current().nextLong(100L, 500L);
        return Duration.ofMillis(baseMillis + jitterMillis);
    }

    private boolean shouldRetry(ExternalApiException exception) {
        Integer status = exception.getHttpStatus();
        return status != null
                && (status == HttpStatus.TOO_MANY_REQUESTS.value()
                || status == HttpStatus.SERVICE_UNAVAILABLE.value());
    }
}
