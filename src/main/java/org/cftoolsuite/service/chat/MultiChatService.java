package org.cftoolsuite.service.chat;

import org.cftoolsuite.domain.chat.MultiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Profile({"alting"})
@Service
public class MultiChatService {

    private final Map<String, ChatClient> chatClients;

    public MultiChatService(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public List<MultiChatResponse> askQuestion(String question) {
        return chatClients.entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    ChatClient chatClient = entry.getValue();

                    try {
                        // Capture start time
                        Instant start = Instant.now();

                        // Call the chat client and get the full response
                        ChatResponse chatResponse = chatClient
                                .prompt()
                                .user(question)
                                .call()
                                .chatResponse();

                        // Capture end time
                        Instant end = Instant.now();

                        // Calculate duration
                        Duration responseDuration = Duration.between(start, end);

                        // Get usage information
                        Usage usage = null;
                        String response = null;
                        if (chatResponse != null) {
                            if (chatResponse.getMetadata() != null) {
                                usage = chatResponse.getMetadata().getUsage();
                            }
                            if (chatResponse.getResult() != null) {
                                response = chatResponse.getResult().getOutput().getContent();
                            }
                        }
                        if (usage == null) {
                            usage = new DefaultUsage(null, null);
                        }

                        // Create enhanced MultiChatResponse
                        return MultiChatResponse.success(modelName, response, usage, responseDuration.toMillis());
                    } catch (Exception e) {
                        String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
                        return MultiChatResponse.failure(modelName, errorMessage);
                    }
                })
                .collect(Collectors.toList());
    }
}
