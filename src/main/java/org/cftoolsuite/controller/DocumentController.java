package org.cftoolsuite.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.service.DocumentIngestionService;
import org.cftoolsuite.service.DocumentSearchService;
import org.cftoolsuite.service.DocumentSummarizationService;
import org.cftoolsuite.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class DocumentController {

    private final FileService fileService;
    private final DocumentIngestionService documentIngestionService;
    private final DocumentSearchService documentSearchService;
    private final DocumentSummarizationService documentSummarizationService;

    public DocumentController(
        FileService fileService, DocumentIngestionService documentIngestionService,
        DocumentSearchService documentSearchService, DocumentSummarizationService documentSummarizationService) {
        this.fileService = fileService;
        this.documentIngestionService = documentIngestionService;
        this.documentSearchService = documentSearchService;
        this.documentSummarizationService = documentSummarizationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("fileName") MultipartFile file) {
        FileMetadata fileMetadata = fileService.uploadFile(file);
        documentIngestionService.ingest(file, fileMetadata);
        return ResponseEntity.ok(fileMetadata);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getFileMetadata(@RequestParam(value = "fileName", required = false) String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return ResponseEntity.ok(fileService.getAllFileMetadata());
        }
        return ResponseEntity.ok(List.of(fileService.getFileMetadata(fileName)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileMetadata>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(documentSearchService.nlSearch(query));
    }

    @GetMapping("/summarize/{fileName}")
    public ResponseEntity<String> summarize(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .body(documentSummarizationService.summarize(fileName));
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileService.downloadFile(fileName));
    }
}
