package com.llm.chats;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private static  final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/v1/chats")
    public Object chat(@RequestBody UserInput userInput){
        log.info("userInput: {}", userInput);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().user(userInput.prompt());

        log.info("requestSpec: {}", requestSpec);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        log.info("responseSpec: {}", responseSpec);

//        log.info("content : {} ",  responseSpec.content());

        return responseSpec.chatResponse();
    }
}
