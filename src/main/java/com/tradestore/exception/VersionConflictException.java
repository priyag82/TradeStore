package com.tradestore.exception;

public class VersionConflictException extends RuntimeException {
    
    public VersionConflictException(String message) {
        super(message);
    }
    
    public VersionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
