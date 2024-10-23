package org.cftoolsuite.domain.crawl;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

public class CrawlCompletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private MultipartFile file;

    public CrawlCompletedEvent(Object source) {
        super(source);
    }

    public CrawlCompletedEvent file(MultipartFile file) {
        this.file = file;
        return this;
    }

    public MultipartFile getFile() {
        return file;
    }
}
