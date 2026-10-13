package com.nexarats.academy.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Global Exception Handler conforming to RFC 7807 (Problem Details for HTTP APIs).
 * Ensures all error responses include correlation IDs and never leak internal stack traces or secrets.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_KEY = "correlationId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setDetail("One or more fields failed validation");
        problem.setType(URI.create("urn:nexarats:error:validation-failed"));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", getCorrelationId());
        problem.setProperty("invalidFields", fieldErrors);

        log.warn("Validation error [correlationId: {}]: {}", getCorrelationId(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode());
        problem.setTitle(ex.getReason() != null ? ex.getReason() : "Request Failed");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("urn:nexarats:error:request-failed"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", getCorrelationId());

        log.warn("Handled response status exception [correlationId: {}]: {}", getCorrelationId(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("You do not have permission to perform this operation");
        problem.setType(URI.create("urn:nexarats:error:forbidden"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", getCorrelationId());

        log.warn("Access denied [correlationId: {}]: {}", getCorrelationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(ex.getMessage() != null ? ex.getMessage() : "Authentication required");
        problem.setType(URI.create("urn:nexarats:error:unauthorized"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", getCorrelationId());

        log.warn("Authentication failed [correlationId: {}]: {}", getCorrelationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("urn:nexarats:error:bad-request"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", getCorrelationId());

        log.warn("Illegal argument exception [correlationId: {}]: {}", getCorrelationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        String correlationId = getCorrelationId();
        log.error("Unhandled server exception [correlationId: {}]", correlationId, ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please contact support with correlation ID: " + correlationId);
        problem.setType(URI.create("urn:nexarats:error:internal-server-error"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("correlationId", correlationId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String getCorrelationId() {
        String correlationId = MDC.get(CORRELATION_KEY);
        return (correlationId != null && !correlationId.isEmpty()) ? correlationId : "N/A";
    }
}
