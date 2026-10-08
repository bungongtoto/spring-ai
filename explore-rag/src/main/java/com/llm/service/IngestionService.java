package com.llm.service;

import com.llm.utils.RagUtiils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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

        String fileExtention  = RagUtiils.getFileExtension(originalFileName);
        switch (fileExtention){
            case "pdf" -> {
                log.info("Ingesting PDF file: {}", originalFileName);
                ingestPdf(ingestType,docSource);
            }
            case "docx" -> {
                log.info("Ingesting DOCX file: {}", originalFileName);
                ingestWordDocs(originalFileName, ingestType, docSource);
            }
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileExtention);
        }

        log.info("Successfully ingested file: {} of type: {}", originalFileName, ingestType);
    }

    private void ingestPdf(String ingestType, Resource faqPdfResource) {
        log.info("Ingesting PDF with ingest type: {}", ingestType);

        List<Document> docs = getPDFDocuments(ingestType, faqPdfResource);

        vectorStore.add(docs);
        log.info("Added {} documents to vector store", docs.size());
    }

    private void ingestWordDocs(String originalFileName, String ingestType, Resource docSource) {
        log.info("Ingesting Word document: {} with ingest type: {}", originalFileName, ingestType);

        List<Document> docs = new TikaDocumentReader(docSource).get();
        vectorStore.add(docs);
        log.info("Added {} documents to vector store", docs.size());
    }
}
