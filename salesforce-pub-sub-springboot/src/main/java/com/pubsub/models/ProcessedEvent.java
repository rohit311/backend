package com.pubsub.models;

import com.google.protobuf.ByteString;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;

@Data
@Slf4j
@Builder
public class ProcessedEvent {
    private String rpcId;
    private String topic;
    private Schema schema;
    private String payload;
    private ByteString replayId;
}
