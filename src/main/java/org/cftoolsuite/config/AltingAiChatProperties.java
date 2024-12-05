package org.cftoolsuite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Profile({"alting"})
@Component
@ConfigurationProperties(prefix = "spring.ai.alting.chat")
public class AltingAiChatProperties {

    private Options options = new Options();

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options {
        private Set<String> models;

        public Set<String> getModels() {
            return models;
        }

        public void setModels(Set<String> models) {
            this.models = models;
        }
    }
}
