package com.example.pdf_summarier.controller;

import com.example.pdf_summarier.rag.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final IngestionService ingestionService;

    public PdfController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Upload PDF and process into vector DB
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {

        // Basic validation
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files allowed");
        }

        // Process file
        ingestionService.ingest(file);

        return ResponseEntity.ok("PDF uploaded & processed successfully");
    }
}
