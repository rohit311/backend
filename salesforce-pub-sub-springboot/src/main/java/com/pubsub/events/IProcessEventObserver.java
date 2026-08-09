package com.pubsub.events;

import com.pubsub.exceptions.EventProcessingException;
import com.pubsub.models.ProcessedEvent;

@FunctionalInterface
public interface IProcessEventObserver {
    void onEvent(String event, ProcessedEvent processedEvent) throws EventProcessingException;
}
