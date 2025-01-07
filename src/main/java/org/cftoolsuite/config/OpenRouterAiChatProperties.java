package org.cftoolsuite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"openrouter"})
@Component
@ConfigurationProperties(prefix = "spring.ai.openrouter.chat")
public class OpenRouterAiChatProperties implements MultiChatProperties {

    private Options options = new Options();

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

}
