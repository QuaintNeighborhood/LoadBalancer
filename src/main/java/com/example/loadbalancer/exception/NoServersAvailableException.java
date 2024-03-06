package com.example.loadbalancer.exception;

public class NoServersAvailableException extends RuntimeException {
    public NoServersAvailableException(final String message) {
        super(message);
    }
}
