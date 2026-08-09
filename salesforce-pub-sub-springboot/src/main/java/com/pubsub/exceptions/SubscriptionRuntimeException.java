package com.pubsub.exceptions;

public class SubscriptionRuntimeException extends RuntimeException {
  public SubscriptionRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}