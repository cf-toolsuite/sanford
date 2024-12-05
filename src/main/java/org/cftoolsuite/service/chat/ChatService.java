package org.cftoolsuite.service.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

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

    public String askQuestion(String question) {
        return askQuestion(question, null);
    }

    public String askQuestion(String question, Map<String, Object> filterMetadata) {
        return chatClient
                .prompt()
                .advisors(RetrievalAugmentationAdvisor
                        .builder()
                        .documentRetriever(
                                ChatServiceHelper.constructDocumentRetriever(vectorStore, filterMetadata).build()
                        )
                        .build())
                .user(question)
                .call()
                .content();
    }
}
