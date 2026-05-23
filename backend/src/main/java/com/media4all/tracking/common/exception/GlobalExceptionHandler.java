package com.media4all.tracking.common.exception;

import com.media4all.tracking.agent.AgentSyncException;
import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.location.LocationSyncException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
