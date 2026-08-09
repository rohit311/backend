package com.pubsub.config;

import com.pubsub.events.IProcessEventObserver;
import com.pubsub.events.ProcessEventManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ObserverConfig {
    private final ProcessEventManager processEventManager;
    private final ApplicationContext applicationContext;
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;

    @PostConstruct
    public void registerObservers() {
        var observers = applicationContext.getBeansOfType(IProcessEventObserver.class).values();
        var activeEvents = salesforceSubscribeConfig.getActiveEvents();

        activeEvents.forEach(event -> {
            boolean observerRegistered = false;

            for (IProcessEventObserver observer : observers) {
                Component componentAnnotation = observer.getClass().getAnnotation(Component.class);
                if (componentAnnotation != null && event.equals(componentAnnotation.value())) {
                    processEventManager.registerObserver(event, observer);
                    observerRegistered = true;
                }
            }

            if (!observerRegistered) {
                log.info("No observer found for event: {}", event);
            }
        });
    }
}