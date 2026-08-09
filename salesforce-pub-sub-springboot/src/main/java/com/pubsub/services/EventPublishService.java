package com.pubsub.services;

import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.PublishResponse;

import io.grpc.CallCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishService {

    private static final String TOPIC = "/event/MyCustomEvent__e";
    private static final String CREATED_BY_ID = "0051P000003ZvNmQAK"; // Replace with the appropriate User ID.

    private final Publish publish;
    private final TopicSchema topicSchema;
    private final SalesforceSessionTokenService salesforceSessionTokenService;

    public PublishResponse publishEvent() throws Exception {
        try {
            var callCredentials = salesforceSessionTokenService.login();
            var schema = fetchSchema(callCredentials);
            var eventMessage = createEventMessage(schema);
            return publish.publishEvent(TOPIC, eventMessage, callCredentials);
        } catch (Exception e) {
            log.error("Failed to publish event to topic: {}", TOPIC, e);
            throw e;
        }
    }

    private Schema fetchSchema(CallCredentials callCredentials) {
        return topicSchema.getSchema(TOPIC, callCredentials);
    }

    private GenericRecord createEventMessage(Schema schema) {
        return new GenericRecordBuilder(schema)
                .set("CreatedDate", System.currentTimeMillis())
                .set("CreatedById", CREATED_BY_ID)
                .build();
    }
}