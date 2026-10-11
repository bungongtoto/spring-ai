package com.llm.vision;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ChatModelCallAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class VisionController {
    private static final Logger log = LoggerFactory.getLogger(VisionController.class);

    private static final String UPLOAD_DIR = "explore-openai/src/main/resources/uploaded_images";

    private final ChatClient chatClient;


    public VisionController(ChatClient.Builder chatClientBuilder) {

        this.chatClient = chatClientBuilder
                .build();
    }

    @PostMapping("/v1/vision")
    public String vision(@RequestBody UserInput userInput) {
        log.info("userInput: {}", userInput);

         var imageResource = new ClassPathResource("files/vision/zebra.jpg");

        UserMessage userMessage = UserMessage
                .builder()
                .text("Explain what do you see in this picture?")
                .media(new Media(MimeTypeUtils.IMAGE_JPEG, imageResource))
                .build();

        ChatClient.CallResponseSpec response = chatClient.prompt(new Prompt(userMessage)).call();

       return response.content();
    }

    @PostMapping("/v2/vision")
    public String visionV2(@RequestParam("file") MultipartFile file, @RequestParam("prompt") String prompt){
        try {
            log.info("File name: {}, File size: {}, Prompt: {}", file.getOriginalFilename(), file.getSize(), prompt);
            UserMessage userMessage = UserMessage
                    .builder()
                    .text("Explain what do you see in this picture?")
                    .media(new Media(MimeTypeUtils.IMAGE_PNG, file.getResource()))
                    .build();

            ChatClient.CallResponseSpec response = chatClient.prompt(new Prompt(userMessage)).call();

            return response.content();
        }catch (Exception e){
            log.error("Error processing vision request", e);
            return "Error processing vision request";
        }
    }
}
