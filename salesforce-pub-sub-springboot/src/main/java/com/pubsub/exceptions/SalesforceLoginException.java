package com.pubsub.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SalesforceLoginException extends RuntimeException {
    public SalesforceLoginException(String message) {
        super(message);
    }

    public SalesforceLoginException(String message, Throwable cause) {
        super(message, cause);
    }


}
