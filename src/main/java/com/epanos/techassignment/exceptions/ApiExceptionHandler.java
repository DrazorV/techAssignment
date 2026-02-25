package com.epanos.techassignment.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest req) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest req) {
        log.error("Conflict error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "CONFLICT", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "VALIDATION_ERROR", msg, req.getRequestURI()));
    }

    /**
     * Handles missing or malformed request body (e.g., missing JSON in PUT/POST request).
     * Returns a friendly 400 Bad Request instead of 500 Internal Server Error.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> malformedRequest(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String message = "Request body is missing or malformed. Please ensure you send valid JSON.";
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
            message = "Request body is required. Please provide a valid JSON body.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("Cannot deserialize")) {
            message = "Invalid JSON format in request body.";
        }
        log.warn("Malformed request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "BAD_REQUEST", message, req.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dbConstraint(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("Database constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "DB_CONSTRAINT", "Database constraint violation", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "INTERNAL_ERROR", "An unexpected error occurred. Please try again later.", req.getRequestURI()));
    }

    @Schema(name = "ApiError", description = "Standard error response")
    public record ApiError(
            @Schema(example = "404") int status,
            @Schema(example = "NOT_FOUND") String code,
            @Schema(example = "Match not found: 123") String message,
            @Schema(example = "/api/matches/123") String path,
            @Schema(example = "2026-02-23T11:25:00+02:00") OffsetDateTime timestamp
    ) {
        static ApiError of(int status, String code, String message, String path) {
            return new ApiError(status, code, message, path, OffsetDateTime.now());
        }
    }
}