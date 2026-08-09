package com.pubsub.health;

import com.pubsub.utils.SalesforceSessionTokenService;
import io.grpc.CallCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for monitoring Salesforce authentication status.
 *
 * This indicator attempts to obtain credentials to verify that:
 * - JWT generation is working
 * - OAuth token exchange is successful
 * - Salesforce authentication endpoint is reachable
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SalesforceAuthHealthIndicator implements HealthIndicator {

    private final SalesforceSessionTokenService sessionTokenService;

    @Override
    public Health health() {
        try {
            CallCredentials credentials = sessionTokenService.login();

            if (credentials == null) {
                return Health.down()
                        .withDetail("auth", "failed")
                        .withDetail("status", "Failed to obtain credentials")
                        .build();
            }

            return Health.up()
                    .withDetail("auth", "authenticated")
                    .withDetail("status", "Successfully authenticated with Salesforce")
                    .withDetail("method", "JWT Bearer Token Flow")
                    .build();

        } catch (Exception e) {
            log.error("Error checking authentication health", e);
            return Health.down()
                    .withException(e)
                    .withDetail("auth", "error")
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
