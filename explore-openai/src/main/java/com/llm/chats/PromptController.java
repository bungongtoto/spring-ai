package com.llm.chats;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class PromptController {


    private static final Logger log = LoggerFactory.getLogger(PromptController.class);
    private final ChatClient chatClient;

    @Value("classpath:/prompt-templates/java-coding-assistant.st")
    private Resource systemTemplateMessage;

    @Value("classpath:/prompt-templates/coding-assistant.st")
    private Resource systemText;


    public PromptController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/v1/prompts")
    public String prompts(@RequestBody UserInput userInput) {
        log.info("UserInput : {} ", userInput);

        String systemMessage = """
                You are a helpful assistant, who can answer java based questions.
                For any other questions, please respond with I don't know in a funny way!
                """;

        SystemMessage sysMessage = new SystemMessage(systemTemplateMessage);
        UserMessage userMessage = new UserMessage(userInput.prompt());

        Prompt promptMessage = new Prompt(List.of(sysMessage,
//                new UserMessage("What's my name?"),
//                new AssistantMessage("I dont know!"),
//                new UserMessage("My name is Kingsley"),
                userMessage));

        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(promptMessage).call();

        return responseSpec.content();
    }


    @PostMapping("/v1/prompts/{language}")
    public String promptsByLanguage(@RequestBody UserInput userInput, @PathVariable String language) {
        log.info("UserInput : {}, language  : {} ", userInput, language);



        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message sysMessage = systemPromptTemplate.createMessage(Map.of("language", language));

        UserMessage userMessage = new UserMessage(userInput.prompt());

        Prompt promptMessage = new Prompt(List.of(sysMessage,
                userMessage));

        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(promptMessage).call();

        return responseSpec.content();
    }

    @PostMapping("/v2/prompts/{language}")
    public Object PromptsByLanguageV2(@PathVariable String language, @RequestBody @Valid UserInput userInput){
        log.info("userInput: {}", userInput);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient
                .prompt()
                .user(userInput.prompt())
                .system(promptSystemSpec -> promptSystemSpec.text(systemText).param("language", language));

        log.info("requestSpec: {}", requestSpec);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        log.info("responseSpec: {}", responseSpec);


        return responseSpec.chatResponse();
    }


}
