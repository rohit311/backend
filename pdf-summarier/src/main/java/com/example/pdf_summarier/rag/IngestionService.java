package com.example.pdf_summarier.rag;

import org.springframework.web.multipart.MultipartFile;

public interface IngestionService {

    void ingest(MultipartFile file);
}
