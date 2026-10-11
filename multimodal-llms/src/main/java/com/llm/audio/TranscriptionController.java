package com.llm.audio;

import com.llm.dto.TranscriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {
    private static final Logger log = LoggerFactory.getLogger(TranscriptionController.class);

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    public TranscriptionController(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
    }

    @PostMapping("/v1/transcription")
    public  ResponseEntity<?> transcription(@RequestParam("file") MultipartFile file){
        Resource audioResource =  file.getResource();

        AudioTranscriptionPrompt transcriptionPrompt = new AudioTranscriptionPrompt(audioResource);

        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionPrompt);

        TranscriptionResponse transcriptionResponse = new TranscriptionResponse(response.getResult().getOutput(), file.getOriginalFilename());

        return new ResponseEntity<>(transcriptionResponse, HttpStatus.OK);
    }

    @PostMapping("/v2/transcription")
    public  ResponseEntity<?> transcriptionV2(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "prompt", required = true) String prompt,
                                              @RequestParam(value = "model", required = true) String model,
                                              @RequestParam(value = "language", required = true) String language,
                                              @RequestParam(value = "response_format", required = true) String responseFormat,
                                              @RequestParam(value = "temperature", required = false) Float temperature

    ){
        log.info("prompt: {}, model: {}, language: {}, response_format: {}, temperature: {}", prompt, model, language, responseFormat, temperature);

        Resource audioResource =  file.getResource();

        temperature = (temperature == null || temperature == 0.0f) ? 1.0f : temperature;

        OpenAiAudioApi.TranscriptResponseFormat responseFormatEnum = OpenAiAudioApi.TranscriptResponseFormat.valueOf(responseFormat);

        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .prompt(prompt)
                .model(model)
                .language(language)
                .responseFormat(responseFormatEnum)
                .temperature(temperature)
                .build();

        log.info("options: {}", options);

        AudioTranscriptionPrompt transcriptionPrompt = new AudioTranscriptionPrompt(audioResource, options);

        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionPrompt);

        TranscriptionResponse transcriptionResponse = new TranscriptionResponse(response.getResult().getOutput(), file.getOriginalFilename());

        return new ResponseEntity<>(transcriptionResponse, HttpStatus.OK);
    }

}
