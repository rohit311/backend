package com.pubsub.exceptions;

/**
 * Exception thrown when there are issues with the gRPC channel.
 *
 * Examples:
 * - Channel creation failures
 * - Connection errors
 * - Channel state issues
 */
public class ChannelException extends PubSubException {

    public ChannelException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ChannelException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
