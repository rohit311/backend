package com.pubsub.exceptions;

/**
 * Exception thrown when schema fetching or parsing fails.
 */
public class SchemaFetchException extends PubSubException {

    public SchemaFetchException(String message, String errorCode) {
        super(message, errorCode);
    }

    public SchemaFetchException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // Keep legacy constructor for backward compatibility
    public SchemaFetchException(String message, Throwable cause) {
        super(message, "SCHEMA_FETCH_ERROR", cause);
    }
}