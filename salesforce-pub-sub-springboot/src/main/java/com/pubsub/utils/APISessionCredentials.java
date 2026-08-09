package com.pubsub.utils;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;


@Slf4j
@Getter
@Setter
public class APISessionCredentials extends CallCredentials {

    // Instance url of the customer org
    private static final Metadata.Key<String> INSTANCE_URL_KEY = keyOf("instanceUrl");
    // Session token of the customer
    private static final Metadata.Key<String> SESSION_TOKEN_KEY = keyOf("accessToken");
    // Tenant Id of the customer org
    private static final Metadata.Key<String> TENANT_ID_KEY = keyOf("tenantId");

    private String instanceURL;
    private String tenantId;
    private String token;


    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        log.debug("API session credentials applied to {}", requestInfo.getMethodDescriptor());
        Metadata headers = new Metadata();
        headers.put(INSTANCE_URL_KEY, instanceURL);
        headers.put(TENANT_ID_KEY, tenantId);
        headers.put(SESSION_TOKEN_KEY, token);
        metadataApplier.apply(headers);
    }

    private static Metadata.Key<String> keyOf(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }

}
