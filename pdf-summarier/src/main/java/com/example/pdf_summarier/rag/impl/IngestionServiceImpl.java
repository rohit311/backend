package com.example.pdf_summarier.rag.impl;

import com.example.pdf_summarier.config.AppProperties;
import com.example.pdf_summarier.rag.IngestionService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IngestionServiceImpl implements IngestionService {

    private final VectorStore vectorStore;
    private final AppProperties appProperties;

    public IngestionServiceImpl(VectorStore vectorStore,
                                AppProperties appProperties) {
        this.vectorStore = vectorStore;
        this.appProperties = appProperties;
    }

    @Override
    public void ingest(MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename();

            // Extract (PDF → Documents)
            PagePdfDocumentReader reader =
                    new PagePdfDocumentReader(
                            new InputStreamResource(file.getInputStream()),
                            PdfDocumentReaderConfig.builder()
                                    .withPagesPerDocument(1)
                                    .build()
                    );

            List<Document> documents = reader.read();

            // Add metadata (VERY IMPORTANT)
            List<Document> enrichedDocs = documents.stream()
                    .map(doc -> new Document(
                            doc.getText(),
                            Map.of(
                                    "fileName", fileName,
                                    "source", "pdf"
                            )
                    ))
                    .collect(Collectors.toList());

            // Transform (chunking using config)
            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunkSize())
                    .build();
            List<Document> chunks = splitter.apply(enrichedDocs);

            // Load into Vector DB
            vectorStore.accept(chunks);

            System.out.println(" Ingested file: " + fileName +
                    " | Chunks: " + chunks.size());

        } catch (Exception e) {
            throw new RuntimeException("PDF ingestion failed", e);
        }
    }
}
