package com.epam.finaltask.dto;

public class ApiResponse<T> {

    private final String statusCode;
    private final String statusMessage;
    private final T results;

    public ApiResponse(String statusCode, String statusMessage, T results) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.results = results;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public T getResults() {
        return results;
    }
}