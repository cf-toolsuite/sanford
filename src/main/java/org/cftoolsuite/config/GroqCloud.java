package org.cftoolsuite.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;

@Configuration
@Conditional(OnGroqCloud.class)
class GroqCloud {

    @Bean
    public EmbeddingModel embeddingModel(PropertyResolver resolver) {
        return new OpenAiEmbeddingModel(
            new OpenAiApi(resolver.getProperty("spring.ai.openai.embedding.base_url"), resolver.getProperty("spring.ai.openai.embedding.api-key")),
            MetadataMode.EMBED,
            OpenAiEmbeddingOptions.builder().withModel(resolver.getProperty("spring.ai.openai.embedding.options.model")).build()
        );
    }
}
