package com.media4all.tracking.common.exception;

public record ErrorResponse(
        ErrorBody error
) {

    public static ErrorResponse of(String code, String message, String details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }

    public record ErrorBody(
            String code,
            String message,
            String details
    ) {
    }
}
