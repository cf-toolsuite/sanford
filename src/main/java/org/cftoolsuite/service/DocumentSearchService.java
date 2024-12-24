package org.cftoolsuite.service;

import org.cftoolsuite.domain.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class DocumentSearchService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSearchService.class);
    private static final int TOP_K = 10;

    private final FileService fileService;
    private final VectorStore store;

    public DocumentSearchService(FileService fileService, VectorStore store) {
        this.fileService = fileService;
        this.store = store;
    }

    public List<FileMetadata> nlSearch(String query) {
        Assert.hasText(query, "Query cannot be null or empty");
        log.debug("Preparing to search with query: {}", query);
        List<Document> candidates = store.similaritySearch(
            SearchRequest.builder().query(query).topK(TOP_K).build()
        );
        log.debug("Found {} documents", candidates.size());
        log.trace("Document content and metadata: {}", candidates);
        Set<String> fileNames = candidates.stream().map(d -> String.valueOf(d.getMetadata().get("file_name"))).collect(Collectors.toSet());
        return fileNames.stream().map(fileService::getFileMetadata).collect(Collectors.toList());
    }

    public List<Document> search(String fileName) {
        Assert.hasText(fileName, "File name cannot be null or empty");
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        log.debug("Preparing to search with fileName: {}", fileName);
        List<Document> candidates = store.similaritySearch(
            SearchRequest.builder().query("Find any document with any word that occurs in this file name: " + fileName).filterExpression(b.eq("file_name", fileName).build()).similarityThresholdAll().build()
        );
        log.debug("Found {} documents", candidates.size());
        log.trace("Document content and metadata: {}", candidates);
        return candidates;
    }

    public void delete(String fileName) {
        List<String> candidateIds = search(fileName).stream().map(Document::getId).collect(Collectors.toList());
        log.debug("Preparing to delete documents with metadata file_name equal to {}", fileName);
        store.delete(candidateIds);
    }

}
