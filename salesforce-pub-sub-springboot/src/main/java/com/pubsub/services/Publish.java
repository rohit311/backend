package com.pubsub.services;

import com.google.protobuf.ByteString;
import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.ProducerEvent;
import com.salesforce.eventbus.protobuf.PublishRequest;
import com.salesforce.eventbus.protobuf.PublishResponse;
import com.salesforce.eventbus.protobuf.SchemaInfo;
import io.grpc.CallCredentials;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class Publish {
    private static final String LOG_PUBLISH_SUCCESS = "Published {} events to topic {} with RPC ID: {}";
    private static final String LOG_PUBLISH_FAILURE = "Failed to publish event to topic {}: {}";

    private final IPubSubService pubSubService;
    private final TopicSchema topicSchema;
    private final SalesforceSessionTokenService salesforceSessionTokenService;


    public PublishResponse publishEvent(final String busTopicName, final GenericRecord event, final CallCredentials callCredentials) throws Exception {
        try {
            SchemaInfo schemaInfo = topicSchema.getSchemaInfo(busTopicName, callCredentials);
            PublishRequest publishRequest = generatePublishRequest(busTopicName, schemaInfo, event);
            PublishResponse publishResponse = pubSubService.publish(publishRequest, callCredentials);

            log.info(LOG_PUBLISH_SUCCESS, publishResponse.getResultsCount(), busTopicName, publishResponse.getRpcId());
            return publishResponse;
        } catch (Exception e) {
            log.error(LOG_PUBLISH_FAILURE, busTopicName, e.getMessage(), e);
            throw e;
        }
    }

    private PublishRequest generatePublishRequest(final String busTopicName, final SchemaInfo schemaInfo, final GenericRecord event) throws IOException {
        ProducerEvent producerEvent = generateProducerEvent(schemaInfo, event);
        return PublishRequest.newBuilder()
                .setTopicName(busTopicName)
                .addEvents(producerEvent)
                .build();
    }

    private ProducerEvent generateProducerEvent(final SchemaInfo schemaInfo, final GenericRecord event) throws IOException {
        byte[] payload = convertToByteArray(event);
        return ProducerEvent.newBuilder()
                .setSchemaId(schemaInfo.getSchemaId())
                .setPayload(ByteString.copyFrom(payload))
                .build();
    }

    private byte[] convertToByteArray(final GenericRecord event) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(event.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(buffer, null);
            writer.write(event, encoder);
            return buffer.toByteArray();
        }
    }
}