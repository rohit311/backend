package com.pubsub.services;

import com.salesforce.eventbus.protobuf.PubSubGrpc;
import com.salesforce.eventbus.protobuf.PublishRequest;
import com.salesforce.eventbus.protobuf.PublishResponse;
import com.salesforce.eventbus.protobuf.SchemaInfo;
import com.salesforce.eventbus.protobuf.TopicInfo;
import com.salesforce.eventbus.protobuf.TopicRequest;
import io.grpc.CallCredentials;

public interface IPubSubService {
    void checkSubscriptionStatus(String topicName, CallCredentials callCredentials);

    String getSchemaJson(String schemaId, CallCredentials callCredentials);

    PubSubGrpc.PubSubBlockingStub pubSubBlockingStub(CallCredentials callCredentials);

    PubSubGrpc.PubSubStub pubSubAsyncStub(CallCredentials callCredentials);

    void logError(String context, Exception e);

    Boolean isChannelShutdown();

    PublishResponse publish(PublishRequest publishRequest, CallCredentials callCredentials);

    TopicInfo getTopicInfo(TopicRequest topicName, CallCredentials callCredentials);

    SchemaInfo getSchemaInfo(String schemaId, CallCredentials callCredentials);

}
