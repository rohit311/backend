package com.example.pdf_summarier.service;

import java.util.List;

import org.springframework.ai.document.Document;

public interface RetrievalService {
    List<Document> retrieve(String query);
}
