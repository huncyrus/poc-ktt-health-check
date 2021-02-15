package com.example.starter;

/**
 * Result of HTTP Client check
 * Special result struct to let return multiple metrics & values from the HTTP client.
 * This object has one simple things to do: get & set data.
 *
 * @version 1.0
 */
public class resultOfHttpClientCheck {
    private boolean available = false;
    private int responseTime = 0;
    private String errorMessage;
    private int statusCode = 200;
    private String url;
    private boolean error = false;

    public boolean isError() {
        return error;
    }

    public void setError(boolean errorMessage) {
        this.error = errorMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String error) {
        this.errorMessage = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
