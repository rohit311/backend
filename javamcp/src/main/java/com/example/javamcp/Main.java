package com.example.javamcp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final PresentationTools presentationTools = new PresentationTools();
    public static void main(String[] args) {
        // Stdio Server Transport (Support for SSE also available)
        var transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        // Sync tool specification
        var syncToolSpecification = getSyncToolSpecification();

        McpServer.sync(transportProvider)
                .serverInfo("presentation_server", "0.0.1")
                .capabilities(
                    McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                        .tools(syncToolSpecification)
                        .build();
        
        logger.info("Starting JavaOne MCP Server...");
    }

    private static McpServerFeatures.SyncToolSpecification getSyncToolSpecification() {
        var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                }
              }
            }
            """;
        
        var syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool("get_presentations", "Get a list of all presentations from JavaOne", schema),
            (exchange, arguments) -> {
                // Tool implementation
                    List<Presentation> presentations = presentationTools.getPresentations();
                    List<McpSchema.Content> contents = new ArrayList<>();
                    for (Presentation presentation : presentations) {
                        contents.add(new McpSchema.TextContent(presentation.toString()));
                    }
                    return new McpSchema.CallToolResult(contents, false);
            }
        );

        return syncToolSpecification;
    }
}