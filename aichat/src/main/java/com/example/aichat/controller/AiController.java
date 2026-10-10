package com.example.aichat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aichat.service.AiChatService;

@RestController
@RequestMapping("/ask")
public class AiController {
    private final AiChatService aiChatService;
    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }
    @GetMapping
    public String ask(@RequestParam String prompt) {
        return aiChatService.askModel(prompt);
    }
}
