package com.example.pdf_summarier.dto;

public class ChatHistoryResponse {
    private String type;
    private String message;

    public ChatHistoryResponse(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
