package com.llm.service;


import com.llm.dtos.GroundingRequest;
import com.llm.dtos.GroundingResponse;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroundingService {

    private static final Logger log = LoggerFactory.getLogger(GroundingService.class);

    private final ChatClient chatClient;

    @Value("classpath:/prompt-templates/RAG-Prompt.st")
    private Resource ragPrompt;


    @Value("classpath:/prompt-templates/RAG-QA-Prompt.st")
    private Resource ragQAPrompt;

    private final PgVectorStore vectorStore;

    private String handbookContent;

    public GroundingService(ChatClient.Builder chatClientBuilder, @Qualifier("qaVectorStore") PgVectorStore pgVectorStore) {
        this.vectorStore = pgVectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    @PostConstruct
    public void init() throws  IOException {
        Path filePath = Paths.get("explore-rag/src/main/resources/docs/technova-handbook.txt");
        handbookContent = Files.readString(filePath);
    }

    public GroundingResponse retrieveAnswer(GroundingRequest groundingRequest) {
        log.info("Retrieving answer for question: {}", groundingRequest.prompt());

        //1. Retrieve relevant documents from vector store
        List<Document> relevantDocs = vectorStore.doSimilaritySearch(
                SearchRequest.builder()
                        .query(groundingRequest.prompt())
                        .build()
        );

        log.info("Found {} relevant documents \n Docs: {}", relevantDocs.size(), relevantDocs);

        String context = relevantDocs
                .stream()
                .filter(Objects::nonNull)
                .filter(doc -> doc.getScore() > 0.8)
                .limit(2)
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        if (StringUtils.isBlank(context)) {
            return new GroundingResponse("I could not find any relevant information to answer your query.");
        }

        log.info("Context used for grounding: {}", context);

        //2. Create a prompt with the question and relevant documents
        PromptTemplate promptTemplate = new PromptTemplate(ragQAPrompt);
        Message message =  promptTemplate.createMessage(
                Map.of( "input", groundingRequest.prompt(), "context", context)
        );
        Prompt prompt = new Prompt(message);

        //3. Send the prompt to the LLM
        String response = chatClient
                .prompt(prompt)
                .call()
                .content();

        //4. Return the response
        log.info("LLM Response: {}", response);
        return  new GroundingResponse(response);
    }

    public GroundingResponse grounding(GroundingRequest groundingRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(ragPrompt);
        Message message =  promptTemplate.createMessage(
                Map.of( "input", groundingRequest.prompt(),
                        "context", handbookContent)
        );

        Prompt prompt = new Prompt(message);

        String response = chatClient
                .prompt(prompt)
                .call()
                .content();
        return  new GroundingResponse(response);
    }



}
