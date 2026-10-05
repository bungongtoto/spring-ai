package com.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
        ingestPdf(faqPdf);
    }

    private void ingestPdf(Resource faqPdfResource) {

        if (ingestionEnabled){
            List<Document> docs = new PagePdfDocumentReader(faqPdfResource).get();

            log.info(" PDF Document Content : {} , size: {} ", docs, docs.size());
            vectorStore.add(docs);
            log.info("Added {} documents to vector store", docs.size());
        }else {
            log.info("Ingestion is disabled");
        }
    }
}
