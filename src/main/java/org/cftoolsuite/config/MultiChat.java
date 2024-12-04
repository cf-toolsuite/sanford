package org.cftoolsuite.config;

import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Profile({"alting"})
@Configuration
public class MultiChat {

    @Bean
    public Map<String, ChatClient> chatClients(
            VectorStore vectorStore,
            OpenAiConnectionProperties connectionProperties,
            OpenAiChatProperties chatProperties,
            WebClient.Builder webClientBuilder,
            RetryTemplate retryTemplate,
            FunctionCallbackContext functionCallbackContext,
            ResponseErrorHandler responseErrorHandler,
            AltingAiChatProperties altChatProperties
    ) {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .defaultHeaders(headers -> headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate"));

        OpenAiApi openAiApi = new OpenAiApi(
                chatProperties.getBaseUrl() != null ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl(),
                chatProperties.getApiKey() != null ? chatProperties.getApiKey() : connectionProperties.getApiKey(),
                restClientBuilder,
                webClientBuilder,
                responseErrorHandler
        );

        return altChatProperties.getOptions().getModels().stream().collect(
                Collectors.toMap(
                    model -> model,
                    model -> {
                        OpenAiChatOptions chatOptions = OpenAiChatOptions.fromOptions(chatProperties.getOptions());
                        chatOptions.setModel(model);
                        OpenAiChatModel openAiChatModel = new OpenAiChatModel(
                                openAiApi,
                                chatOptions,
                                functionCallbackContext,
                                retryTemplate
                        );
                        // Create ChatClient with similar configuration to original service
                        return ChatClient.builder(openAiChatModel)
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
                                        new QuestionAnswerAdvisor(vectorStore),
                                        new SimpleLoggerAdvisor()
                                )
                                .build();
                    }
                )
        );
    }

}