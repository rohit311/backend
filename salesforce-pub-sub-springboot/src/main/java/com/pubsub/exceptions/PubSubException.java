package com.pubsub.exceptions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception for all Pub/Sub related errors.
 *
 * Provides structured error information including error codes and contextual data.
 */
@Getter
public class PubSubException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> context;

    public PubSubException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    public PubSubException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    /**
     * Add contextual information to the exception.
     *
     * @param key   the context key
     * @param value the context value
     * @return this exception for method chaining
     */
    public PubSubException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
}
