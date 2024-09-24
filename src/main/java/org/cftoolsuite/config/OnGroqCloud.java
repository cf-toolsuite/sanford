package org.cftoolsuite.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class OnGroqCloud implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        return activeProfiles.contains("groq-cloud");
    }
}
