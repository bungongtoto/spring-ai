package com.llm.chats;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@RestController
@Validated
public class ChatController {
    private static  final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/v1/chats")
    public Object chat(@RequestBody @Valid UserInput userInput){
        log.info("userInput: {}", userInput);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient
                .prompt()
                .user(userInput.prompt());

        log.info("requestSpec: {}", requestSpec);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        log.info("responseSpec: {}", responseSpec);

//        log.info("content : {} ",  responseSpec.content());

        return responseSpec.chatResponse();
    }


    @PostMapping("/v2/chats")
    public Object chatV2(@RequestBody @Valid UserInput userInput){
        log.info("userInput: {}", userInput);

        String systemMessage = """
                You are a helpful assistant, who can answer java based questions.
                For any other questions, please respond with I don't know in a funny way!
                """;

        ChatClient.ChatClientRequestSpec requestSpec = chatClient
                .prompt()
                .user(userInput.prompt())
                .system(systemMessage);

        log.info("requestSpec: {}", requestSpec);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        log.info("responseSpec: {}", responseSpec);

//        log.info("content : {} ",  responseSpec.content());

        return responseSpec.chatResponse();
    }

    /**
     *  used to provide streaming response
     * @param userInput
     * @return
     */
    @PostMapping("/v1/chats/stream")
    public Flux<String> chatWithStream(@RequestBody @Valid UserInput userInput){
       return chatClient
                .prompt()
                .user(userInput.prompt())
                .stream()
                .content()
               .doOnNext(s -> log.info("s : {} ", s))
               .doOnComplete(() -> log.info("completed"))
               //.onErrorReturn("Error occurred while processing the request")
               .onErrorResume(throwable -> {
                   log.error("Error occurred: {}" , throwable.getMessage());
                   //return Flux.just("Error occurred while processing the request: " + throwable.getMessage());
                   return Flux.error(new RuntimeException("Error occurred while processing the request: " + throwable.getMessage()));
               });
    }
}
