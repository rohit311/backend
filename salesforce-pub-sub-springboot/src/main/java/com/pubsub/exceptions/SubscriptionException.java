package com.pubsub.exceptions;

public class SubscriptionException extends Exception {
    public SubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionException(String message) {
        super(message);
    }
}