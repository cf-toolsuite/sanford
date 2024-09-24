package org.cftoolsuite.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @see https://github.com/spring-projects/spring-ai/issues/512
public class Http {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder ->
            restClientBuilder
                .requestFactory(
                    ClientHttpRequestFactories
                        .get(
                            ClientHttpRequestFactorySettings.DEFAULTS
                                .withConnectTimeout(Duration.ofMinutes(2))
                                .withReadTimeout(Duration.ofMinutes(2))
                        )
                );
    }
}
