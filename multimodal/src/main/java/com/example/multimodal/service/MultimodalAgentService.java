package com.example.multimodal.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.multimodal.agent.ExpenseTools;
import com.example.multimodal.model.dto.ReceiptSummary;
import com.example.multimodal.util.PdfToImageConverter;

@Service 
public class MultimodalAgentService {

    private final ChatClient visionClient;
    private final ChatClient agentClient;
    private final PdfToImageConverter pdfConverter;


    public MultimodalAgentService(ChatClient.Builder builder, ExpenseTools tools, PdfToImageConverter pdfConverter) {
        // 1. Vision client: purely for structured extraction from images
        this.visionClient = builder.clone().build();
        this.pdfConverter = pdfConverter;

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

        String contentType = Objects.requireNonNullElse(file.getContentType(), "image/png");
        List<Media> mediaList = buildMediaList(file, contentType);

        

        // Step 1: Extract structured data from the receipt image
        ReceiptSummary extracted = visionClient.prompt()
                .user(u -> u
                    .text(instruction)
                    .media(mediaList.toArray(new Media[0])))
                .call()
                .entity(ReceiptSummary.class);

        // Step 2: Hand extracted data to the tool-calling agent (triggers logExpense -> Postgres)
        String toolOutcome = agentClient.prompt()
                .user("Log this extracted receipt expense: Merchant=" + extracted.merchant()
                        + ", Amount=" + extracted.totalAmount()
                        + ", Currency=" + extracted.currency()
                        + ", Items=" + extracted.items())
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

    private List<Media> buildMediaList(MultipartFile file, String contentType) throws IOException {
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            List<Resource> pages = pdfConverter.convertToPngResources(file.getBytes());
            return pages.stream()
                    .map(page -> new Media(MimeTypeUtils.IMAGE_PNG, page))
                    .toList();
        }

        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
        return List.of(new Media(mimeType, new ByteArrayResource(file.getBytes())));
    }
}
