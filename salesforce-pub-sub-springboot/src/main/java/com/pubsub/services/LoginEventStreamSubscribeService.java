package com.pubsub.services;

import com.pubsub.config.SalesforceSubscribeConfig;
import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.ReplayPreset;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginEventStreamSubscribeService {

    private static final String TOPIC = "/data/AccountChangeEvent";
    private static final int EVENTS_PER_FETCH = 5;

    private final Subscribe subscribe;
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;
    private final SalesforceSessionTokenService salesforceSessionTokenService;

    @PostConstruct
    @ConditionalOnProperty(value = "salesforce-subscribe-config.event-listening-on", havingValue = "true")
    public void startStreamEventSubscription() {
        if (isTopicActive()) {
            startSubscription();
        } else {
            logInactiveTopicWarning();
        }
    }

    private boolean isTopicActive() {
        List<String> activeEvents = salesforceSubscribeConfig.getActiveEvents();
        return activeEvents != null && activeEvents.contains(TOPIC);
    }

    private void startSubscription() {
        try {
            log.info("Starting subscription for topic: {}", TOPIC);
            subscribe.startSubscription(TOPIC, EVENTS_PER_FETCH, ReplayPreset.LATEST, salesforceSessionTokenService.login());
        } catch (Exception e) {
            log.error("Failed to start subscription for topic: {}", TOPIC, e);
        }
    }

    private void logInactiveTopicWarning() {
        log.warn("Topic {} is not active in the configuration. Subscription will not start.", TOPIC);
    }
}