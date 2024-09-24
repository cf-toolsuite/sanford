package org.cftoolsuite.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cftoolsuite.domain.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;


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
        List<Document> candidates = store.similaritySearch(SearchRequest.query(query).withTopK(TOP_K));;
        log.trace("Found these: {}", candidates);
        Set<String> fileNames = candidates.stream().map(d -> String.valueOf(d.getMetadata().get("file_name"))).collect(Collectors.toSet());
        return fileNames.stream().map(f -> fileService.getFileMetadata(f)).collect(Collectors.toList());
    }

    public List<Document> search(String fileName) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> candidates = store.similaritySearch(SearchRequest.defaults().withFilterExpression(b.eq("file_name", fileName).build()).withSimilarityThresholdAll());
        log.trace("Found these: {}", candidates);
        return candidates;
    }

}
