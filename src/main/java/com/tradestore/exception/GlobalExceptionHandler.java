package com.tradestore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidTradeException.class)
    public ResponseEntity<Object> handleInvalidTradeException(
            InvalidTradeException ex, WebRequest request) {
        
        Map<String, Object> body = createProblemDetails(
            HttpStatus.BAD_REQUEST,
            "Invalid Trade",
            ex.getMessage(),
            "https://tradestore.com/errors/invalid-trade",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<Object> handleVersionConflictException(
            VersionConflictException ex, WebRequest request) {
        
        Map<String, Object> body = createProblemDetails(
            HttpStatus.CONFLICT,
            "Version Conflict",
            ex.getMessage(),
            "https://tradestore.com/errors/version-conflict",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> body = createProblemDetails(
            HttpStatus.BAD_REQUEST,
            "Invalid Argument",
            ex.getMessage(),
            "https://tradestore.com/errors/invalid-argument",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> body = createProblemDetails(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred",
            "https://tradestore.com/errors/internal-server-error",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createProblemDetails(
            HttpStatus status,
            String title,
            String detail,
            String type,
            String instance) {
        
        Map<String, Object> problemDetails = new LinkedHashMap<>();
        problemDetails.put("type", URI.create(type));
        problemDetails.put("title", title);
        problemDetails.put("status", status.value());
        problemDetails.put("detail", detail);
        problemDetails.put("instance", URI.create(instance));
        problemDetails.put("timestamp", Instant.now());
        
        return problemDetails;
    }
}
