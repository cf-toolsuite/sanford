package org.cftoolsuite.service.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatModel model, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(model)
                .defaultSystem("""
                    You are an AI assistant with access to a specific knowledge base
                    Follow these guidelines:
                        Only use information from the provided context.
                        If the answer is not in the context, state that you don't have sufficient information.
                        Do not use any external knowledge or make assumptions beyond the given data.
                        Cite the relevant parts of the context in your responses including the source and origin.
                        Respond in a clear, concise manner without editorializing.
                    """
                )
        		.defaultAdvisors(
                        new VectorStoreChatMemoryAdvisor(vectorStore),
        				new QuestionAnswerAdvisor(vectorStore),
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