package org.cftoolsuite.service.crawl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.domain.crawl.CrawlCompletedEvent;
import org.cftoolsuite.service.DocumentIngestionService;
import org.cftoolsuite.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CrawlListener implements ApplicationListener<CrawlCompletedEvent> {

    private static Logger log = LoggerFactory.getLogger(CrawlListener.class);

    private final DocumentIngestionService documentIngestionService;
    private final FileService fileService;

    public CrawlListener(FileService fileService, DocumentIngestionService documentIngestionService) {
        this.fileService = fileService;
        this.documentIngestionService = documentIngestionService;
    }

    public void accept(MultipartFile file) {
        FileMetadata fileMetadata = fileService.uploadFile(file);
        documentIngestionService.ingest(file, fileMetadata);
        try {
            log.debug("Attempting to delete " + file.getOriginalFilename());
            Path filePath = Path.of(file.getResource().getURI());
            Files.deleteIfExists(filePath);
        } catch (IOException ioe) {
            log.error("Problem deleting " + file.getOriginalFilename());
        }
    }

    @Override
    public void onApplicationEvent(CrawlCompletedEvent event) {
        accept(event.getFile());
    }

}
