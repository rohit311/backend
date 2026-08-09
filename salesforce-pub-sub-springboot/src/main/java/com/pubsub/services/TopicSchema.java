package com.pubsub.services;

import com.pubsub.utils.SalesforceSessionTokenService;
import com.salesforce.eventbus.protobuf.SchemaInfo;
import com.salesforce.eventbus.protobuf.TopicInfo;
import com.salesforce.eventbus.protobuf.TopicRequest;
import io.grpc.CallCredentials;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.Schema;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class TopicSchema {

    private static final String LOG_SCHEMA_FETCHED = "Schema of topic {}: {}";
    private static final String LOG_TOPIC_RPC_ID = "GetTopic Call RPC ID: {}";
    private static final String LOG_SCHEMA_RPC_ID = "GetSchema Call RPC ID: {}";

    private final IPubSubService pubSubService;
    private final SalesforceSessionTokenService salesforceSessionTokenService;
    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();

    public Schema getSchema(String topicName, CallCredentials callCredentials) {
        return schemaCache.computeIfAbsent(topicName, key -> {
            try {
                TopicInfo topicInfo = fetchTopicInfo(key, callCredentials);
                SchemaInfo schemaInfo = fetchSchemaInfo(topicInfo.getSchemaId(), callCredentials);

                Schema schema = new Schema.Parser().parse(schemaInfo.getSchemaJson());
                log.info(LOG_SCHEMA_FETCHED, key, schema.toString());
                return schema;
            } catch (Exception e) {
                log.error("Failed to fetch schema for topic: {}", key, e);
                throw e;
            }
        });
    }

    public SchemaInfo getSchemaInfo(String topicName, CallCredentials callCredentials) {
        try {
            TopicInfo topicInfo = fetchTopicInfo(topicName, callCredentials);
            return fetchSchemaInfo(topicInfo.getSchemaId(), callCredentials);
        } catch (Exception e) {
            log.error("Failed to fetch schema info for topic: {}", topicName, e);
            throw e;
        }
    }

    private TopicInfo fetchTopicInfo(String topicName, CallCredentials callCredentials) {
        try {
            TopicInfo topicInfo = pubSubService.getTopicInfo(
                    TopicRequest.newBuilder().setTopicName(topicName).build(), callCredentials);
            logRpcId(LOG_TOPIC_RPC_ID, topicInfo.getRpcId());
            return topicInfo;
        } catch (Exception e) {
            log.error("Failed to fetch topic info for topic: {}", topicName, e);
            throw e;
        }
    }

    private SchemaInfo fetchSchemaInfo(String schemaId, CallCredentials callCredentials) {
        try {
            SchemaInfo schemaInfo = pubSubService.getSchemaInfo(schemaId, callCredentials);
            logRpcId(LOG_SCHEMA_RPC_ID, schemaInfo.getRpcId());
            return schemaInfo;
        } catch (Exception e) {
            log.error("Failed to fetch schema info for schema ID: {}", schemaId, e);
            throw e;
        }
    }

    private void logRpcId(String message, String rpcId) {
        log.info(message, rpcId);
    }
}
