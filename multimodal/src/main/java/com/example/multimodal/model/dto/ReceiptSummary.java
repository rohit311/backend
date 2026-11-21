package com.example.multimodal.model.dto;

import java.util.List;

public record ReceiptSummary(
        String merchant,
        String date,
        List<String> items,
        double totalAmount,
        String currency,
        String actionTaken
) {}
