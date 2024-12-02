package org.cftoolsuite.service.chat;

import org.cftoolsuite.domain.chat.MultiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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
                        String response = chatClient
                                .prompt()
                                .user(question)
                                .call()
                                .content();

                        return MultiChatResponse.success(modelName, response);
                    } catch (Exception e) {
                        return MultiChatResponse.failure(
                                modelName,
                                e.getMessage() != null ? e.getMessage() : "Unknown error occurred"
                        );
                    }
                })
                .collect(Collectors.toList());
    }
}
