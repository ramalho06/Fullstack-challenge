package com.media4all.tracking.external;

import java.time.Duration;

public class ExternalApiException extends RuntimeException {

    private final Integer httpStatus;
    private final String responseBody;
    private final Duration retryAfter;

    public ExternalApiException(String message) {
        this(message, null, null, null);
    }

    public ExternalApiException(String message, Integer httpStatus, String responseBody) {
        this(message, httpStatus, responseBody, null);
    }

    public ExternalApiException(String message, Integer httpStatus, String responseBody, Duration retryAfter) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.retryAfter = retryAfter;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
