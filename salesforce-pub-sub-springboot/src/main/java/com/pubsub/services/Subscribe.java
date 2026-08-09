package com.pubsub.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.pubsub.config.SalesforceSubscribeConfig;
import com.pubsub.events.ProcessEventManager;
import com.pubsub.exceptions.SchemaFetchException;
import com.pubsub.models.ProcessedEvent;
import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.ConsumerEvent;
import com.salesforce.eventbus.protobuf.FetchRequest;
import com.salesforce.eventbus.protobuf.FetchResponse;
import com.salesforce.eventbus.protobuf.ReplayPreset;
import io.grpc.CallCredentials;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class Subscribe {

    // Constants for log messages
    private static final String LOG_RECEIVED_BATCH = "Received batch of {} {} with RPC ID: {}";
    private static final String LOG_RETRY_SUBSCRIPTION = "Retrying {} subscription";
    private static final String LOG_SUBSCRIPTION_COMPLETED = "Call completed by server. Closing Subscription.";
    private static final String ERROR_FETCH_SCHEMA = "Failed to fetch schema for ID: ";
    private static final String ERROR_PROCESS_EVENT = "Error processing event: {}";
    private static final String ERROR_SUBSCRIPTION = "Error during {} subscription";

    private final IPubSubService pubSubService;
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;
    private final SalesforceSessionTokenService salesforceSessionTokenService;
    private final TopicSchema topicSchema;
    private final ObjectMapper objectMapper;
    private final ProcessEventManager processEventManager;
    private final ApplicationContext applicationContext;
    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();

    public void startSubscription(String topic, int batchSize, ReplayPreset replayPreset, CallCredentials callCredentials) {
        try {
            pubSubService.checkSubscriptionStatus(topic, callCredentials);
            topicSchema.getSchema(topic, callCredentials);
            fetchEvents(batchSize, topic, replayPreset, callCredentials);
            logChannelStatus(topic);
        } catch (Exception e) {
            log.error("Failed to start subscription for topic: {}", topic, e);
        }
    }

    private void fetchEvents(int batchSize, String topic, ReplayPreset replayPreset, CallCredentials callCredentials) {
        StreamObserverHolder<FetchRequest> fetchRequestHolder = new StreamObserverHolder<>();
        StreamObserver<FetchResponse> fetchResponseObserver = createResponseObserver(batchSize, topic, fetchRequestHolder, callCredentials);
        fetchRequestHolder.setStreamObserver(pubSubService.pubSubAsyncStub(callCredentials).subscribe(fetchResponseObserver));
        sendFetchRequest(fetchRequestHolder.getStreamObserver(), topic, batchSize, replayPreset);
    }

    private StreamObserver<FetchResponse> createResponseObserver(int batchSize, String topic, StreamObserverHolder<FetchRequest> fetchRequestHolder, CallCredentials callCredentials) {
        return new StreamObserver<>() {
            @Override
            public void onNext(FetchResponse fetchResponse) {
                handleFetchResponse(fetchResponse, batchSize, topic, fetchRequestHolder.getStreamObserver(), callCredentials);
            }

            @Override
            public void onError(Throwable t) {
                handleSubscriptionError(t, batchSize, topic);
            }

            @Override
            public void onCompleted() {
                log.info(LOG_SUBSCRIPTION_COMPLETED);
            }
        };
    }

    private void handleFetchResponse(FetchResponse fetchResponse, int batchSize, String topic, StreamObserver<FetchRequest> fetchRequestStreamObserver, CallCredentials callCredentials) {
        if (!fetchResponse.getEventsList().isEmpty()) {
            log.info(LOG_RECEIVED_BATCH, fetchResponse.getEventsList().size(), topic, fetchResponse.getRpcId());
            fetchResponse.getEventsList().forEach(event -> processEventSafely(event, topic, fetchResponse.getRpcId(), callCredentials));

            if (fetchResponse.getPendingNumRequested() == 0) {
                sendFetchRequest(fetchRequestStreamObserver, topic, batchSize, ReplayPreset.LATEST);
            }
        }
    }

    private void processEventSafely(ConsumerEvent event, String topic, String rpcId, CallCredentials callCredentials) {
        try {
            processEvent(event, topic, rpcId, callCredentials);
        } catch (Exception e) {
            log.error(ERROR_PROCESS_EVENT, e.getMessage(), e);
        }
    }

    private void processEvent(ConsumerEvent event, String topic, String rpcId, CallCredentials callCredentials) throws Exception {
        ProcessedEvent processedEvent = createProcessedEvent(event, topic, rpcId, callCredentials);
        notifyObservers(topic, processedEvent);
    }

    private ProcessedEvent createProcessedEvent(ConsumerEvent event, String topic, String rpcId, CallCredentials callCredentials) throws IOException {
        Schema writerSchema = getSchema(event.getEvent().getSchemaId(), callCredentials);
        GenericRecord receivedRecord = deserialize(writerSchema, event.getEvent().getPayload());

        return ProcessedEvent.builder()
                .topic(topic)
                .rpcId(rpcId)
                .schema(writerSchema)
                .payload(receivedRecord.toString())
                .replayId(event.getReplayId())
                .build();
    }

    private Schema getSchema(String schemaId, CallCredentials callCredentials) {
        return schemaCache.computeIfAbsent(schemaId, id -> {
            try {
                String schemaJson = pubSubService.getSchemaJson(schemaId, callCredentials);
                return new Schema.Parser().parse(schemaJson);
            } catch (Exception e) {
                log.error(ERROR_FETCH_SCHEMA + "{}", schemaId, e);
                throw new SchemaFetchException(ERROR_FETCH_SCHEMA + schemaId, e);
            }
        });
    }

    private void notifyObservers(String topic, ProcessedEvent processedEvent) throws Exception {
        processEventManager.notifyObservers(topic, processedEvent);
    }

    private GenericRecord deserialize(Schema schema, ByteString payload) throws IOException {
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        try (ByteArrayInputStream in = new ByteArrayInputStream(payload.toByteArray())) {
            BinaryDecoder decoder = DecoderFactory.get().directBinaryDecoder(in, null);
            return reader.read(null, decoder);
        }
    }

    private void handleSubscriptionError(Throwable t, int batchSize, String topic) {
        log.error(ERROR_SUBSCRIPTION, topic, t);
        try {
            retrySubscription(batchSize, topic);
        } catch (Exception e) {
            log.error("Failed to retry subscription for topic: {}", topic, e);
        }
    }

    private void retrySubscription(int batchSize, String topic) {
        log.info(LOG_RETRY_SUBSCRIPTION, topic);
        startSubscription(topic, batchSize, ReplayPreset.LATEST, salesforceSessionTokenService.login());
    }

    private void sendFetchRequest(StreamObserver<FetchRequest> fetchRequestStreamObserver, String topic, int batchSize, ReplayPreset replayPreset) {
        FetchRequest fetchRequest = FetchRequest.newBuilder()
                .setNumRequested(batchSize)
                .setTopicName(topic)
                .setReplayPreset(replayPreset)
                .build();
        fetchRequestStreamObserver.onNext(fetchRequest);
    }

    private void logChannelStatus(String topic) {
        Boolean isChannelShutdown = pubSubService.isChannelShutdown();
        log.info("Channel is {} for {}", Boolean.TRUE.equals(isChannelShutdown) ? "shutdown" : "open", topic);
    }

    @Data
    private static class StreamObserverHolder<T> {
        private StreamObserver<T> streamObserver;
    }
}