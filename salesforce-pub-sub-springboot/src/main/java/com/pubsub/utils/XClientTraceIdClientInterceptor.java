package com.pubsub.utils;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class XClientTraceIdClientInterceptor implements ClientInterceptor {
    private static final Metadata.Key<String> X_CLIENT_TRACE_ID = Metadata.Key.of("x-client-trace-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String xClientTraceId = UUID.randomUUID().toString();
                headers.put(X_CLIENT_TRACE_ID, xClientTraceId);

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                }, headers);
            }
        };
    }
}
