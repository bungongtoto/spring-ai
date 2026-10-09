package com.llm.image;

import com.llm.dto.ImageInput;
import com.llm.dto.UserInput;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;

import static com.llm.utils.ImageUtil.decodeBase64ToImage;
import static com.llm.utils.ImageUtil.saveImageToFile;

/**
 * https://platform.openai.com/docs/api-reference/images/create
 */
@RestController
public class ImageController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    public OpenAiImageModel openAiImageModel;

    public ImageController(OpenAiImageModel openAiImageModel) {
        this.openAiImageModel = openAiImageModel;
    }

    @PostMapping("/v1/images")
    public  ImageResponse images (@RequestBody UserInput userInput) {
        log.info("userInput: {}", userInput);
        ImageResponse response = openAiImageModel.call(new ImagePrompt(userInput.prompt()));
        String b64Json = response.getResults().get(0).getOutput().getB64Json();

        BufferedImage image = decodeBase64ToImage(b64Json);

        String filePath = "output_image.png";
        boolean success = saveImageToFile(image, "png", filePath);

        if (success) {
            log.info("Image saved successfully to: {}", filePath);
        } else{
            log.error("Failed to save image");
        }

        return response;
    }




}
