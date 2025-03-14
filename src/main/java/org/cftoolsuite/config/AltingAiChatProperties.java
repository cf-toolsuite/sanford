package org.cftoolsuite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"alting"})
@Component
@ConfigurationProperties(prefix = "spring.ai.alting.chat")
public class AltingAiChatProperties implements MultiChatProperties {

    private Options options = new Options();

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

}
