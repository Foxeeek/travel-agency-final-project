package com.epam.finaltask.exception;

public class ConflictException extends LocalizedException {
    public ConflictException(String message ) {
        super(message, null);
    }
    public ConflictException(String message, String key, Object... args) {
        super(message, key, args);
    }


}
