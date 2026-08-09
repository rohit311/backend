package com.pubsub.services;

import com.pubsub.config.PubSubConfiguration;
import com.pubsub.exceptions.PublishException;
import com.pubsub.exceptions.SchemaFetchException;
import com.pubsub.utils.SalesforceSessionTokenService;
import com.pubsub.utils.XClientTraceIdClientInterceptor;
import com.salesforce.eventbus.protobuf.*;
import io.grpc.*;
import io.grpc.stub.AbstractStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@RequiredArgsConstructor
@Service
public class PubSubService implements IPubSubService {

    private final SalesforceSessionTokenService salesforceSessionTokenService;
    private final PubSubConfiguration config;
    private ManagedChannel managedChannel;

    @PostConstruct
    private void initializeChannel() {
        managedChannel = createManagedChannel();
    }

    @Override
    public void checkSubscriptionStatus(String topicName, CallCredentials callCredentials) {
        CallCredentials credentials = Optional.ofNullable(callCredentials)
                .orElseGet(salesforceSessionTokenService::login);

        try {
            TopicRequest request = TopicRequest.newBuilder().setTopicName(topicName).build();
            TopicInfo topicInfo = pubSubBlockingStub(credentials).getTopic(request);

            if (topicInfo.getCanSubscribe()) {
                log.info("Subscription available for topic: {} (RPC Id: {})", topicInfo.getTopicName(), topicInfo.getRpcId());
            } else {
                throw new IllegalArgumentException("Topic " + topicInfo.getTopicName() + " is not available for subscription");
            }
        } catch (Exception e) {
            logError("Error fetching topic details for topic: " + topicName, e);
        }
    }

    @Override
    public String getSchemaJson(String schemaId, CallCredentials callCredentials) {
        try {
            SchemaRequest request = SchemaRequest.newBuilder().setSchemaId(schemaId).build();
            return pubSubBlockingStub(callCredentials).getSchema(request).getSchemaJson();
        } catch (StatusRuntimeException e) {
            logError("Error fetching schema for schemaId: " + schemaId, e);
            throw new SchemaFetchException(
                "Failed to fetch schema: " + schemaId,
                "SCHEMA_FETCH_ERROR",
                e
            );
        }
    }

    @Override
    @Retryable(retryFor = { StatusRuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public PublishResponse publish(PublishRequest publishRequest, CallCredentials callCredentials) {
        try {
            return pubSubBlockingStub(callCredentials).publish(publishRequest);
        } catch (StatusRuntimeException e) {
            logError("Error publishing message", e);
            throw new PublishException(
                "Failed to publish event",
                "PUBLISH_ERROR",
                e
            );
        }
    }

    @Override
    @Retryable(retryFor = { StatusRuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public TopicInfo getTopicInfo(TopicRequest topicName, CallCredentials callCredentials) {
        return pubSubBlockingStub(callCredentials).getTopic(topicName);
    }

    @Override
    @Retryable(retryFor = { StatusRuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public SchemaInfo getSchemaInfo(String schemaId, CallCredentials callCredentials) {
        return pubSubBlockingStub(callCredentials).getSchema(SchemaRequest.newBuilder().setSchemaId(schemaId).build());
    }

    @Scheduled(fixedRateString = "${pubsub.event-processing.channel-health-check-interval-ms}")
    void checkChannelState() {
        log.info("Current channel status: {}", getOrCreateManagedChannel().getState(true));
    }

    @Override
    public void logError(String context, Exception e) {
        log.error(context, e);
        if (e instanceof StatusRuntimeException statusRuntimeException) {
            Optional.ofNullable(statusRuntimeException.getTrailers())
                    .ifPresent(trailers -> trailers.keys().forEach(key ->
                            log.error("[Trailer] Key: {}, Value: {}", key,
                                    trailers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))));
        }
    }

    @Override
    public Boolean isChannelShutdown() {
        return managedChannel != null && managedChannel.isShutdown();
    }

    @Override
    public PubSubGrpc.PubSubBlockingStub pubSubBlockingStub(CallCredentials callCredentials) {
        return createStub(PubSubGrpc::newBlockingStub, callCredentials);
    }

    @Override
    public PubSubGrpc.PubSubStub pubSubAsyncStub(CallCredentials callCredentials) {
        return createStub(PubSubGrpc::newStub, callCredentials);
    }

    private <T extends AbstractStub<T>> T createStub(Function<Channel, T> stubFactory, CallCredentials callCredentials) {
        Channel interceptedChannel = ClientInterceptors.intercept(getOrCreateManagedChannel(), new XClientTraceIdClientInterceptor());
        return stubFactory.apply(interceptedChannel).withCallCredentials(callCredentials);
    }

    private ManagedChannel getOrCreateManagedChannel() {
        if (managedChannel == null || managedChannel.isShutdown() || managedChannel.isTerminated()) {
            managedChannel = createManagedChannel();
        }
        return managedChannel;
    }

    private ManagedChannel createManagedChannel() {
        return ManagedChannelBuilder
                .forAddress(config.getGrpc().getHost(), config.getGrpc().getPort())
                .idleTimeout(config.getGrpc().getChannelIdleTimeoutMinutes(), TimeUnit.MINUTES)
                .build();
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down gRPC channel");
        if (managedChannel != null && !managedChannel.isShutdown()) {
            try {
                managedChannel.shutdown();
                long timeoutSeconds = config.getGrpc().getShutdownTimeoutSeconds();
                if (!managedChannel.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    log.warn("Channel did not terminate gracefully after {} seconds, forcing shutdown", timeoutSeconds);
                    managedChannel.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Channel shutdown interrupted", e);
                managedChannel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}