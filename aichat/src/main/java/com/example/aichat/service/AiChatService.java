package com.example.aichat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {
    private final ChatClient chatClient;
    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    public String askModel(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }
}
