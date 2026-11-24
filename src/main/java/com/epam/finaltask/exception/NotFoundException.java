package com.epam.finaltask.exception;

public class NotFoundException extends LocalizedException {
    public NotFoundException(String message) {
        super(message, null);
    }

    public NotFoundException(String message, String key, Object... args) {
        super(message, key, args);
    }

}
