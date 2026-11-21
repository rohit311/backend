package com.example.multimodal;

import java.io.IOException;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service 
public class MultimodalAgentService {

    private final ChatClient visionClient;
    private final ChatClient agentClient;


    public MultimodalAgentService(ChatClient.Builder builder, ExpenseTools tools) {
        // 1. Vision client: purely for structured extraction from images
        this.visionClient = builder.clone().build();

        // 2. Agent client: has tools attached, no JSON format constraint blocking tool_calls
        this.agentClient = builder.clone()
                .defaultSystem("""
                    You are an expense-logging agent.
                    Always call the 'logExpense' tool using the provided receipt details,
                    then summarize the result.
                    """)
                .defaultTools(tools)
                .build();
    }

    public @Nullable ReceiptSummary analyzeAndAct(MultipartFile file, String instruction) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        MimeType mimeType = MimeTypeUtils.parseMimeType(
                Objects.requireNonNullElse(file.getContentType(), "image/png")
        );
        ByteArrayResource imageResource = new ByteArrayResource(file.getBytes());

        // Step 1: Extract structured data from the receipt image
        ReceiptSummary extracted = visionClient.prompt()
                .user(u -> u
                    .text(instruction)
                    .media(mimeType, imageResource))
                .call()
                .entity(ReceiptSummary.class);

        // Step 2: Hand extracted data to the tool-calling agent (triggers logExpense -> Postgres)
        String toolOutcome = agentClient.prompt()
                .user("Log this extracted receipt expense: Merchant=" + extracted.merchant()
                        + ", Amount=" + extracted.totalAmount()
                        + ", Currency=" + extracted.currency())
                .call()
                .content();

        // Return combined structured record with the real DB confirmation
        return new ReceiptSummary(
                extracted.merchant(),
                extracted.date(),
                extracted.items(),
                extracted.totalAmount(),
                extracted.currency(),
                toolOutcome
        );
    }
}
