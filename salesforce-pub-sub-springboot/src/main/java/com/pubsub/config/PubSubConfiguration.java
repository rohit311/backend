package com.pubsub.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Centralized configuration for Salesforce Pub/Sub integration.
 *
 * Externalizes all hardcoded values for better configurability and testability.
 * All values have sensible defaults but can be overridden via application.properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pubsub")
@Validated
public class PubSubConfiguration {

    @Valid
    private GrpcConfig grpc = new GrpcConfig();

    @Valid
    private EventProcessingConfig eventProcessing = new EventProcessingConfig();

    @Valid
    private SchemaConfig schema = new SchemaConfig();

    /**
     * gRPC channel configuration for Salesforce Pub/Sub API.
     */
    @Data
    public static class GrpcConfig {
        /**
         * Salesforce Pub/Sub API hostname.
         */
        @NotBlank
        private String host = "api.pubsub.salesforce.com";

        /**
         * Salesforce Pub/Sub API port.
         */
        @Min(1)
        @Max(65535)
        private int port = 7443;

        /**
         * Maximum time in seconds to wait for graceful channel shutdown.
         */
        @Min(1)
        private long shutdownTimeoutSeconds = 30;

        /**
         * Channel idle timeout in minutes before automatic cleanup.
         */
        @Min(1)
        private long channelIdleTimeoutMinutes = 30;
    }

    /**
     * Event processing configuration.
     */
    @Data
    public static class EventProcessingConfig {
        /**
         * Number of threads in the event processing pool.
         */
        @Min(1)
        private int threadPoolSize = 10;

        /**
         * Number of events to fetch per batch from Salesforce.
         */
        @Min(1)
        private int batchSize = 5;

        /**
         * Interval in milliseconds for channel health checks.
         */
        @Min(1000)
        private long channelHealthCheckIntervalMs = 3600000; // 1 hour
    }

    /**
     * Schema caching configuration.
     */
    @Data
    public static class SchemaConfig {
        /**
         * Time-to-live for cached schemas in minutes.
         */
        @Min(1)
        private long cacheTtlMinutes = 60;

        /**
         * Interval in milliseconds for schema cache cleanup.
         */
        @Min(60000)
        private long cacheCleanupIntervalMs = 300000; // 5 minutes
    }
}
