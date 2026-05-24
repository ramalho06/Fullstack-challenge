package com.media4all.tracking.common.exception;

import com.media4all.tracking.agent.AgentSyncException;
import com.media4all.tracking.checkin.CheckInSyncException;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.geofence.GeofenceSyncException;
import com.media4all.tracking.location.LocationSyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "RESOURCE_NOT_FOUND",
                        exception.getMessage(),
                        exception.getResourceName() + " id=" + exception.getResourceId()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("VALIDATION_ERROR", "Request validation failed", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("BAD_REQUEST", exception.getMessage(), null));
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException exception) {
        HttpStatus status = exception.getHttpStatus() != null
                && exception.getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS.value()
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status)
                .body(ErrorResponse.of("EXTERNAL_API_ERROR", exception.getMessage(), exception.getResponseBody()));
    }

    @ExceptionHandler(AgentSyncException.class)
    public ResponseEntity<ErrorResponse> handleAgentSyncException(AgentSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "AGENT_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(LocationSyncException.class)
    public ResponseEntity<ErrorResponse> handleLocationSyncException(LocationSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "LOCATION_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(CheckInSyncException.class)
    public ResponseEntity<ErrorResponse> handleCheckInSyncException(CheckInSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "CHECK_IN_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(GeofenceSyncException.class)
    public ResponseEntity<ErrorResponse> handleGeofenceSyncException(GeofenceSyncException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "GEOFENCE_SYNC_ERROR",
                        exception.getMessage(),
                        "syncExecutionId=" + exception.getSyncExecutionId()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "Unexpected internal error", null));
    }
}
