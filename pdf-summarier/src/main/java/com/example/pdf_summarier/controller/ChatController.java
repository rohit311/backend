package com.example.pdf_summarier.controller;

import com.example.pdf_summarier.dto.ChatHistoryResponse;
import com.example.pdf_summarier.dto.ChatRequest;
import com.example.pdf_summarier.dto.ChatResponse;
import com.example.pdf_summarier.service.ChatService;
import com.example.pdf_summarier.service.MemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final MemoryService memoryService;


    public ChatController(ChatService chatService, MemoryService memoryService) {
        this.chatService = chatService;
        this.memoryService = memoryService;
    }

    /**
     * Chat endpoint using JSON request
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        System.out.println("Rohit :: "+ request.getSessionId());
        String result = chatService.chat(request.getSessionId(), request.getMessage());

        return new ChatResponse(result);
    }

    /**
     * Get chat history
     */
    @GetMapping("/history")
    public List<ChatHistoryResponse> getHistory(@RequestParam String sessionId) {

        return memoryService.getHistory(sessionId);
    }
}
