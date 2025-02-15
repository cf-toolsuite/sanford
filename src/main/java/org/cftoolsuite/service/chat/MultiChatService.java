package org.cftoolsuite.service.chat;

import org.cftoolsuite.domain.chat.FilterMetadata;
import org.cftoolsuite.domain.chat.MultiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Profile({"alting","openrouter"})
@Service
public class MultiChatService {

    private final Map<String, ChatClient> chatClients;
    private final VectorStore vectorStore;

    public MultiChatService(Map<String, ChatClient> chatClients, VectorStore vectorStore) {
        this.chatClients = chatClients;
        this.vectorStore = vectorStore;
    }

    public List<MultiChatResponse> askQuestion(String question) {
        return askQuestion(question, null);
    }

    public List<MultiChatResponse> askQuestion(String question, List<FilterMetadata> filterMetadata) {
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
                                .advisors(
                                        RetrievalAugmentationAdvisor
                                                .builder()
                                                .documentRetriever(
                                                        ChatServiceHelper.constructDocumentRetriever(vectorStore, filterMetadata).build()
                                                )
                                                .build()
                                )
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
                                response = chatResponse.getResult().getOutput().getText();
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
