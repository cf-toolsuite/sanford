package org.cftoolsuite.domain.crawl;

import java.nio.file.Path;

import org.springframework.context.ApplicationEvent;

public class CrawlCompletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Path filePath;

    public CrawlCompletedEvent(Object source) {
        super(source);
    }

    public CrawlCompletedEvent filePath(Path filePath) {
        this.filePath = filePath;
        return this;
    }

    public Path getFilePath() {
        return filePath;
    }
}
