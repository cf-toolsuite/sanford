package org.cftoolsuite;

import java.util.Map;

import org.cftoolsuite.domain.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AppPropertiesLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AppPropertiesLogger.class);
    private static final String NEW_LINE = System.lineSeparator();
    private static final String TAB = "\t";

    private final AppProperties appProperties;

    public AppPropertiesLogger(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        StringBuilder logMessage = new StringBuilder("Supported Content Types:");
        Map<String, String> supportedContentTypes = appProperties.supportedContentTypes();
        for (Map.Entry<String, String> entry : supportedContentTypes.entrySet()) {
            logMessage.append(NEW_LINE)
                      .append(TAB)
                      .append(entry.getKey())
                      .append(" -> ")
                      .append(entry.getValue());
        }
        log.info(logMessage.toString());
    }
}
