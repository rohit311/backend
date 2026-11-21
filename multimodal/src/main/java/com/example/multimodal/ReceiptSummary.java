package com.example.multimodal;

import java.util.List;

public record ReceiptSummary(
        String merchant,
        String date,
        List<String> items,
        double totalAmount,
        String currency,
        String actionTaken
) {}
