package com.media4all.tracking.external;

public class ExternalApiException extends RuntimeException {

    private final Integer httpStatus;
    private final String responseBody;

    public ExternalApiException(String message) {
        this(message, null, null);
    }

    public ExternalApiException(String message, Integer httpStatus, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
