package com.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngestionService implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    @Value("classpath:docs/Flexora_FAQ.pdf")
    private Resource faqPdf;

    @Value("${ingestion.enabled:false}")
    private boolean ingestionEnabled;

    public IngestionService(@Qualifier(value = "qaVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    @Override
    public void run(String... args) throws Exception {
       if (ingestionEnabled){
           ingestPdf("page",faqPdf);
           log.info("CommandLine Ingestion is enabled and completed ingestion");
       }else {
           log.info("CommandLine Ingestion is disabled");
       }
    }

    private void ingestPdf(String ingestType, Resource faqPdfResource) {
        log.info("Ingesting PDF with ingest type: {}", ingestType);

        List<Document> docs = getPDFDocuments(ingestType, faqPdfResource);

        vectorStore.add(docs);
        log.info("Added {} documents to vector store", docs.size());
    }

    private List<Document> getPDFDocuments(String ingestType, Resource pdfResource){
        try {
            return switch (ingestType) {
                case "page" -> new PagePdfDocumentReader(pdfResource).get();
                case "paragraph" -> new ParagraphPdfDocumentReader(pdfResource).get();
                default -> throw new IllegalArgumentException("Invalid ingest type: " + ingestType);
            };
        } catch (Exception e) {
            log.error("Error reading PDF document: {}", e.getMessage(), e);
            throw new RuntimeException("Error while reading PDF document",e);
        }

    }

    public  void ingest(byte[] fileContent, String originalFileName, String ingestType) {

        log.info("IngestionService is invoked - ingesting file: {} of type: {}", originalFileName, ingestType);

        Resource docSource = new ByteArrayResource(fileContent){

            @Override
            public String getFilename() {
                return originalFileName;
            }

        };

        ingestPdf(ingestType,docSource);

        log.info("Successfully ingested file: {} of type: {}", originalFileName, ingestType);
    }
}
