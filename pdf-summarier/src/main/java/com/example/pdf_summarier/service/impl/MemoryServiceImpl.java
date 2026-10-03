package com.example.pdf_summarier.service.impl;

import org.springframework.stereotype.Service;

import com.example.pdf_summarier.dto.ChatHistoryResponse;
import com.example.pdf_summarier.service.MemoryService;

import java.util.*;




@Service
public class MemoryServiceImpl implements MemoryService {

    private final Map<String, List<ChatHistoryResponse>> memory = new HashMap<>();

    @Override
    public void addMessage(String sessionId, String type, String message) {
        memory.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(new ChatHistoryResponse(type, message));
    }

    @Override
    public List<ChatHistoryResponse> getHistory(String sessionId) {
        return memory.getOrDefault(sessionId, new ArrayList<>());
    }

    @Override
    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}
