package com.llm.audio;

import com.llm.dto.TTSInput;
import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.llm.utils.AudioUtil.writeMP3ToFile;

@RestController
public class TextToSpeechController {

    private static final Logger log = LoggerFactory.getLogger(TextToSpeechController.class);
    public static String OUTPUT_PATH = "multimodal-llms/src/main/resources/files/audio";

    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    public TextToSpeechController(OpenAiAudioSpeechModel openAiAudioSpeechModel) {
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
    }

    @PostMapping("/v1/tts")
    public ResponseEntity<String> textToSpeech(@RequestBody @Valid UserInput ttsInput) {
        log.info("Received TTS request for text: {}", ttsInput);

        SpeechPrompt speechPrompt = new SpeechPrompt(ttsInput.prompt());

        SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);

        byte[] responseBytes = speechResponse.getResult().getOutput();

        writeMP3ToFile(responseBytes, OUTPUT_PATH + "/speech.mp3");

        return ResponseEntity.ok("Speech generated successfully");

    }

    @PostMapping("/v2/tts")
    public ResponseEntity<String> textToSpeechV2(@RequestBody @Valid TTSInput ttsInput) {
        log.info("Received TTS request for text: {}", ttsInput);

        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions
                .builder()
                .model(ttsInput.model().value)
                .voice(ttsInput.voice())
                .responseFormat(ttsInput.responseFormat())
                .speed(ttsInput.speed())
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(ttsInput.prompt(), speechOptions);

        SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);

        byte[] responseBytes = speechResponse.getResult().getOutput();

        String outputPath = String.format(
                "%s/%s.%s",
                OUTPUT_PATH,
                ttsInput.fileName(),
                ttsInput.responseFormat()
        );

        writeMP3ToFile(responseBytes, outputPath);

        return ResponseEntity.ok("Speech generated successfully");

    }



}
