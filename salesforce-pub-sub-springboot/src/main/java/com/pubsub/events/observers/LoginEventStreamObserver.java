package com.pubsub.events.observers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubsub.events.IProcessEventObserver;
import com.pubsub.models.ProcessedEvent;
import com.pubsub.services.EventPublishService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



@Slf4j
@Data
@Component(value = "/data/AccountChangeEvent")
@RequiredArgsConstructor
public class LoginEventStreamObserver implements IProcessEventObserver {
    private static final String TOPIC = "/data/AccountChangeEvent";
    private final ObjectMapper objectMapper;
    private final EventPublishService eventPublishService;

    @Override
    public void onEvent(String event, ProcessedEvent processedEvent) {
        if (event.equals(TOPIC)) {
            handleEvent(processedEvent);
        }
    }

    private void handleEvent(ProcessedEvent processedEvent) {
        log.info("Received event: {}", processedEvent.getPayload());
        // Process the event as needed
        // For example, you can send it to a message queue or perform some action based on the event
        // Example: sendToMessageQueue(processedEvent);
    }
}