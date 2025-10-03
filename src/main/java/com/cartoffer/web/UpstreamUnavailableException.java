package com.cartoffer.web;

public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
