package org.cftoolsuite.config;

import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Profile({"groq-cloud","openai"})
@Configuration
// @see https://github.com/spring-projects/spring-ai/issues/372#issuecomment-2242650500
public class Chat {

    @Bean
    public OpenAiChatModel chatModel(
            OpenAiConnectionProperties connectionProperties,
            OpenAiChatProperties chatProperties,
            WebClient.Builder webClientBuilder,
            RetryTemplate retryTemplate,
            FunctionCallbackContext functionCallbackContext,
            ResponseErrorHandler responseErrorHandler
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

        return new OpenAiChatModel(
                openAiApi,
                chatProperties.getOptions(),
                functionCallbackContext,
                retryTemplate
        );
    }

}