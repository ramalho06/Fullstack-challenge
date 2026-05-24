package com.media4all.tracking.common.exception;

public record ApiErrorResponse(
        ApiError error
) {

    public static ApiErrorResponse of(String code, String message) {
        return of(code, message, null);
    }

    public static ApiErrorResponse of(String code, String message, String details) {
        return new ApiErrorResponse(new ApiError(code, message, details));
    }

    public record ApiError(
            String code,
            String message,
            String details
    ) {
    }
}
