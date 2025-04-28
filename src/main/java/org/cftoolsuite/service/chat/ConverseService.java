package org.cftoolsuite.service.chat;

import org.cftoolsuite.domain.chat.AudioResponse;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionProperties;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Profile({"openai"})
@Service
public class ConverseService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final OpenAiAudioTranscriptionProperties transcriptionProperties;
    private final OpenAiAudioSpeechProperties speechProperties;
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public ConverseService(
            ChatModel model,
            VectorStore vectorStore,
            OpenAiAudioTranscriptionProperties transcriptionProperties,
            OpenAiAudioSpeechProperties speechProperties,
            OpenAiAudioTranscriptionModel transcriptionModel,
            OpenAiAudioSpeechModel speechModel) {
        this.chatClient = ChatClient.builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor())
                .build();
        this.vectorStore = vectorStore;
        this.transcriptionProperties = transcriptionProperties;
        this.speechProperties = speechProperties;
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    public AudioResponse respondToAudioRequest(byte[] audioBytes) {
        Resource audioFile = new ByteArrayResource(audioBytes);

        // Transcribe audio request
        OpenAiAudioTranscriptionOptions transcriptionOptions =
                OpenAiAudioTranscriptionOptions.builder()
                    .language(transcriptionProperties.getOptions().getLanguage())
                    .prompt(transcriptionProperties.getOptions().getPrompt())
                    .temperature(transcriptionProperties.getOptions().getTemperature())
                    .responseFormat(transcriptionProperties.getOptions().getResponseFormat())
                    .build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse inquiry = transcriptionModel.call(transcriptionRequest);

        // Get an answer to the inquiry
        String responseToInquiry = constructRequest(inquiry.getResult().getOutput()).call().content();

        // Text to speech
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .model(speechProperties.getOptions().getModel())
                .voice(speechProperties.getOptions().getVoice())
                .responseFormat(speechProperties.getOptions().getResponseFormat())
                .speed(speechProperties.getOptions().getSpeed())
                .build();
        SpeechPrompt speechPrompt = new SpeechPrompt(responseToInquiry, speechOptions);
        SpeechResponse speechResponse = speechModel.call(speechPrompt);

        // Response includes text and audio (as byte[])
        return new AudioResponse(responseToInquiry, speechResponse.getResult().getOutput());
    }

    private ChatClient.ChatClientRequestSpec constructRequest(String question) {
        return chatClient
                .prompt()
                .advisors(RetrievalAugmentationAdvisor
                        .builder()
                        .documentRetriever(
                                ChatServiceHelper.constructDocumentRetriever(vectorStore, null).build()
                        )
                        .build())
                .user(question);
    }
}
