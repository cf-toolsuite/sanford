package org.cftoolsuite.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Chat {

    @Bean
    public ChatClient chatClient(ChatModel model) {
        return
            ChatClient
                .builder(model)
                .defaultSystem("""
                    Summarize the following text into a concise paragraph that captures the main points and essential details without losing important information.
                    The summary should be as short as possible while remaining clear and informative.
                    Use bullet points or numbered lists to organize the information if it helps to clarify the meaning.
                    Focus on the key facts, events, and conclusions.
                    Avoid including minor details or examples unless they are crucial for understanding the main ideas.
                    """
                )
                .build();
    }

}