package org.cftoolsuite.service.chat;

import org.cftoolsuite.domain.chat.FilterMetadata;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatModel model, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(model)
        		.defaultAdvisors(
        				new SimpleLoggerAdvisor())
        		.build();
        this.vectorStore = vectorStore;
    }

    public String respondToQuestion(String question) {
        return constructRequest(question, null)
                .call()
                .content();
    }

    public String respondToQuestion(String question, List<FilterMetadata> filterMetadata) {
        return constructRequest(question, filterMetadata)
                .call()
                .content();
    }

    public Flux<String> streamResponseToQuestion(String question) {
        return constructRequest(question, null)
                .stream()
                .content();
    }

    public Flux<String> streamResponseToQuestion(String question, List<FilterMetadata> filterMetadata) {
        return constructRequest(question, filterMetadata)
                .stream()
                .content();
    }

    private ChatClient.ChatClientRequestSpec constructRequest(String question, List<FilterMetadata> filterMetadata) {
        return chatClient
                .prompt()
                .advisors(RetrievalAugmentationAdvisor
                        .builder()
                        .documentRetriever(
                                ChatServiceHelper.constructDocumentRetriever(vectorStore, filterMetadata).build()
                        )
                        .build())
                .user(question);
    }
}
