package org.cftoolsuite.service.crawl;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.domain.crawl.CrawlCompletedEvent;
import org.cftoolsuite.service.DocumentIngestionService;
import org.cftoolsuite.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class CrawlListener implements ApplicationListener<CrawlCompletedEvent> {

    private static Logger log = LoggerFactory.getLogger(CrawlListener.class);

    private final DocumentIngestionService documentIngestionService;
    private final FileService fileService;

    public CrawlListener(FileService fileService, DocumentIngestionService documentIngestionService) {
        this.fileService = fileService;
        this.documentIngestionService = documentIngestionService;
    }

    public void accept(Path filePath) {
        FileMetadata fileMetadata = fileService.uploadFile(filePath);
        documentIngestionService.ingest(filePath, fileMetadata, false);
        cleanup(filePath);
    }

    @Override
    public void onApplicationEvent(CrawlCompletedEvent event) {
        accept(event.getFilePath());
    }

    private void cleanup(Path filePath) {
        try {
            log.debug("Attempting to delete " + filePath.toString());
            Files.deleteIfExists(filePath);
        } catch (IOException ioe) {
            log.error("Problem deleting " + filePath.toString());
        }
    }

    @PreDestroy
    private void onShutdown() {
        Path storageFolderParent = Path.of(String.join(System.getProperty("file.separator"), System.getProperty("java.io.tmpdir"), "crawler4j"));
        try {
            Files.walkFileTree(storageFolderParent, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ioe) {
            log.error("Problem deleting " + storageFolderParent.toString() + ". You may need to manually execute rm -Rf " + storageFolderParent.toString() + " to remove any orphaned files.");
        }
    }

}
