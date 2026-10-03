package com.example.pdf_summarier.service;

import java.util.List;
import com.example.pdf_summarier.dto.ChatHistoryResponse;

public interface MemoryService {

    void addMessage(String sessionId, String type, String message);

    List<ChatHistoryResponse> getHistory(String sessionId);

    void clear(String sessionId);
}
