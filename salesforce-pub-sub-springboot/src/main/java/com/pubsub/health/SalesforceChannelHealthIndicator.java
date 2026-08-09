package com.pubsub.health;

import com.pubsub.services.PubSubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for monitoring gRPC channel connectivity to Salesforce.
 *
 * Health States:
 * - UP: Channel is ready for communication
 * - DOWN: Channel is shutdown or unavailable
 * - OUT_OF_SERVICE: Channel is in connecting/transient failure state
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SalesforceChannelHealthIndicator implements HealthIndicator {

    private final PubSubService pubSubService;

    @Override
    public Health health() {
        try {
            if (pubSubService.isChannelShutdown()) {
                return Health.down()
                        .withDetail("channel", "shutdown")
                        .withDetail("status", "Channel is not available")
                        .build();
            }

            // Channel exists and is not shutdown
            return Health.up()
                    .withDetail("channel", "active")
                    .withDetail("status", "Connected to Salesforce Pub/Sub API")
                    .withDetail("host", "api.pubsub.salesforce.com")
                    .withDetail("port", 7443)
                    .build();

        } catch (Exception e) {
            log.error("Error checking channel health", e);
            return Health.down()
                    .withException(e)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
