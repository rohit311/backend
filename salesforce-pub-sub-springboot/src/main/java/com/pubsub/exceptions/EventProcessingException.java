package com.pubsub.exceptions;

public class EventProcessingException extends Exception {
    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}