package com.media4all.tracking.common.exception;

import com.media4all.tracking.agent.AgentSyncException;
import com.media4all.tracking.checkin.CheckInSyncException;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.geofence.GeofenceSyncException;
import com.media4all.tracking.location.LocationSyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        "RESOURCE_NOT_FOUND",
                        exception.getMessage(),
                        exception.getResourceName() + " id=" + exception.getResourceId()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of("VALIDATION_ERROR", "Validation failed", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("BAD_REQUEST", exception.getMessage(), null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        "BAD_REQUEST",
                        "Missing required request parameter",
                        exception.getParameterName()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        "BAD_REQUEST",
                        "Invalid request parameter",
                        exception.getName()
                ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("CONFLICT", exception.getMessage(), null));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalApiException(ExternalApiException exception) {
        HttpStatus status = exception.getHttpStatus() != null
                && exception.getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS.value()
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of("EXTERNAL_API_ERROR", exception.getMessage(), exception.getResponseBody()));
    }

    @ExceptionHandler(AgentSyncException.class)
    public ResponseEntity<ApiErrorResponse> handleAgentSyncException(AgentSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "AGENT_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(LocationSyncException.class)
    public ResponseEntity<ApiErrorResponse> handleLocationSyncException(LocationSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "LOCATION_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(CheckInSyncException.class)
    public ResponseEntity<ApiErrorResponse> handleCheckInSyncException(CheckInSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "CHECK_IN_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(GeofenceSyncException.class)
    public ResponseEntity<ApiErrorResponse> handleGeofenceSyncException(GeofenceSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "GEOFENCE_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR", "Unexpected internal error", null));
    }
}
