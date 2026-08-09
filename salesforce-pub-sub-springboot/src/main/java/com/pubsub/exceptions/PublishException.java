package com.pubsub.exceptions;

/**
 * Exception thrown when event publishing fails.
 */
public class PublishException extends PubSubException {

    public PublishException(String message, String errorCode) {
        super(message, errorCode);
    }

    public PublishException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
