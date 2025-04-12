package com.ice.musicdistribution.api;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;

/**
 * Global exception handler for consistent API error responses
 */
@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    /**
     * Handle IllegalArgumentException (invalid inputs)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid input: {}", e.getMessage());

        ErrorResponse error = new ErrorResponse(
                "INVALID_INPUT",
                e.getMessage(),
                Instant.now(clock)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle IllegalStateException (business rule violations)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("Business rule violation: {}", e.getMessage());

        ErrorResponse error = new ErrorResponse(
                "BUSINESS_RULE_VIOLATION",
                e.getMessage(),
                Instant.now(clock)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error", e);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                Instant.now(clock)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO
     */
    @Data
    public static class ErrorResponse {
        private final String code;
        private final String message;
        private final Instant timestamp;
    }
}