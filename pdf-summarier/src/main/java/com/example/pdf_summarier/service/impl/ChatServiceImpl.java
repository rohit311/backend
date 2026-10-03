package com.example.pdf_summarier.service.impl;

import com.example.pdf_summarier.service.ChatService;
import com.example.pdf_summarier.service.RetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_CONTEXT_CHUNKS = 10;
    private static final int MIN_CONTEXT_LENGTH = 50;

    private final RetrievalService retrievalService;
    private final ChatClient chatClient;

    public ChatServiceImpl(RetrievalService retrievalService,
                           ChatClient chatClient) {
        this.retrievalService = retrievalService;
        this.chatClient = chatClient;
    }

    @Override
    public String chat(String sessionId, String question) {
        System.out.println("In service "+ sessionId);
        log.info("Incoming question: {}", question);
        log.info("Session id: {}", sessionId);

        // Step 1: Rewrite query using LLM (handles pronouns + semantics)
        String refinedQuery = rewriteQuery(sessionId, question);

        // Step 2: Retrieve documents
        List<Document> docs = retrieveWithFallback(refinedQuery, question);

        if (docs == null || docs.isEmpty()) {
            log.warn("No documents found after retrieval.");
            return "I don't know";
        }

        // Step 3: Build context
        String context = buildContext(docs);

        log.debug("Context used:\n{}", context);
        String fallbackSessionId = UUID.randomUUID().toString();

        // Step 4: Generate answer
        String response = chatClient.prompt()
                .user(buildPrompt(context, question))
                .advisors(a -> a.param("chat_memory_conversation_id", fallbackSessionId))
                .call()
                .content();

        log.info("Raw response: {}", response);

        return sanitize(response);
    }

    // Query rewriting using LLM
    private String rewriteQuery(String sessionId, String question) {

        try {
            String refined = chatClient.prompt()
                    .user("""
                        Rewrite this query for semantic search over a resume.

                        Instructions:
                        - Resolve pronouns like "his", "her", "their"
                        - Assume the subject is the candidate in the resume
                        - Expand technical terms into related keywords
                        - Keep it concise and optimized for search

                        Examples:
                        "what is his name" → "candidate name in the resume"
                        "what are his skills" → "skills of the candidate in the resume"
                        "microservices experience" → "Spring Boot, REST APIs, distributed systems experience"

                        Question: %s
                        """.formatted(question))
                    .advisors(a -> a.param("conversationId", sessionId))
                    .call()
                    .content();

            log.info("Refined query: {}", refined);
            return refined;

        } catch (Exception e) {
            log.warn("Query rewrite failed, using original query.");
            return question;
        }
    }

    // Retrieval with fallback strategy
    private List<Document> retrieveWithFallback(String refinedQuery, String originalQuery) {

        List<Document> docs = retrievalService.retrieve(refinedQuery);

        log.info("Retrieved documents using refined query: {}",
                docs != null ? docs.size() : 0);

        docs = filterDocs(docs);

        if (isWeakContext(docs)) {
            log.warn("Weak context detected, retrying with original query.");

            docs = retrievalService.retrieve(originalQuery);

            log.info("Retrieved documents using fallback query: {}",
                    docs != null ? docs.size() : 0);

            docs = filterDocs(docs);
        }

        return docs;
    }

    // Filter irrelevant or very small chunks
    private List<Document> filterDocs(List<Document> docs) {
        if (docs == null) return List.of();

        return docs.stream()
                .filter(d -> d.getText() != null && d.getText().length() > MIN_CONTEXT_LENGTH)
                .distinct()
                .limit(MAX_CONTEXT_CHUNKS)
                .collect(Collectors.toList());
    }

    // Detect weak context
    private boolean isWeakContext(List<Document> docs) {
        if (docs == null || docs.isEmpty()) return true;

        return docs.stream()
                .map(Document::getText)
                .allMatch(text -> text.length() < MIN_CONTEXT_LENGTH);
    }

    // Build context string
    private String buildContext(List<Document> docs) {
        return docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

    // Prompt for final answer generation
    private String buildPrompt(String context, String question) {
        return """
            You are an expert assistant analyzing a resume.

            Always assume the question refers to the candidate in the resume.

            Answer the question using ONLY the given context.

            Rules:
            - Do not hallucinate
            - If exact answer is not present but related information exists, infer carefully
            - Prefer semantic understanding over exact keyword match
            - If no relevant information is found, respond with: I don't know

            Answer Style:
            - Start with "From the resume"
            - Keep the answer concise and natural
            - If inferred, mention "based on experience with"

            Context:
            %s

            Question:
            %s

            Answer:
            """.formatted(context, question);
    }

    // Clean response
    private String sanitize(String response) {

        if (response == null || response.isBlank()) {
            return "I don't know";
        }

        String clean = response.trim();
        String lower = clean.toLowerCase(Locale.ROOT);

        if (lower.contains("not mentioned") ||
                lower.contains("no information") ||
                lower.contains("cannot find")) {
            return "I don't know";
        }

        return clean;
    }
}
