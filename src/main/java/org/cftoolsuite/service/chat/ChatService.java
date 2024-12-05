package org.cftoolsuite.service.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatModel model, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(model)
        		.defaultAdvisors(
                        RetrievalAugmentationAdvisor
                                .builder()
                                .documentRetriever(
                                        VectorStoreDocumentRetriever
                                                .builder()
                                                .vectorStore(vectorStore)
                                                .build()
                                )
                                .build(),
        				new SimpleLoggerAdvisor())
        		.build();
    }

    public String askQuestion(String question) {
        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}
