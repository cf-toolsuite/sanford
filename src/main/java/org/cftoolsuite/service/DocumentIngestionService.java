package org.cftoolsuite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.cftoolsuite.domain.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final ChatModel chatModel;
    private final VectorStore store;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public DocumentIngestionService(ChatModel chatModel, VectorStore store, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.store = store;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    public void ingest(Path filePath, FileMetadata metadata, boolean keywordsEnabled) {
        ingest(new FileSystemResource(filePath), metadata, keywordsEnabled);
    }

    public void ingest(MultipartFile file, FileMetadata fileMetadata, boolean keywordsEnabled) {
        ingest(file.getResource(), fileMetadata, keywordsEnabled);
    }

    protected void ingest(Resource resource, FileMetadata fileMetadata, boolean keywordsEnabled) {
        String fileName = fileMetadata.fileName();
        String fileExtension = fileMetadata.fileExtension();
        log.info("-- Ingesting file: {}", fileName);
        List<Document> documents = null;
        switch (fileExtension.toLowerCase()) {
            case "md":
                documents = loadMarkdown(fileName, resource);
                break;
            case "pdf":
                documents = loadPdf(resource);
                break;
            case "log":
                documents = loadText(fileName, resource);
                break;
            case "txt":
                documents = loadText(fileName, resource);
                break;
            case "csv":
                documents = loadText(fileName, resource);
                break;
            case "tsv":
                documents = loadText(fileName, resource);
                break;
            case "json":
                documents = loadJson(resource);
                break;
            case "xml":
                documents = loadXml(resource);
                break;
            case "html":
                documents = loadTika(resource);
                break;
            case "htm":
                documents = loadTika(resource);
                break;
            case "doc":
                documents = loadTika(resource);
                break;
            case "docx":
                documents = loadTika(resource);
                break;
            case "ppt":
                documents = loadTika(resource);
                break;
            case "pptx":
                documents = loadTika(resource);
                break;
            default:
                throw new IllegalArgumentException("Filename [" + fileName + "] contains an unsupported file extension [" + fileExtension + "]");
        }

        TokenTextSplitter splitter = new TokenTextSplitter();
        if (keywordsEnabled) {
            KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
            List<Document> keywordEnrichedDocuments = enricher.apply(documents);
            store.accept(splitter.apply(keywordEnrichedDocuments));
        } else {
            store.accept(splitter.apply(documents));
        }
    }

    private List<Document> loadXml(Resource resource) {
        try {
            JsonNode node = xmlMapper.readTree(resource.getInputStream());
            byte[] jsonBytes = objectMapper.writeValueAsBytes(node);
            return loadJson(new ByteArrayResource(jsonBytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to either read XML or write XML content as JSON", e);
        }
    }

    private List<Document> loadTika(Resource resource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file_name", resource.getFilename());
        List<Document> documents = tikaDocumentReader.read();
        List<Document> enrichedDocuments = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> customMetadata = new HashMap<>(metadata);
            customMetadata.putAll(document.getMetadata());
            enrichedDocuments.add(new Document(document.getText(), customMetadata));
        }
        return enrichedDocuments;
    }

    private List<Document> loadJson(Resource resource) {
        if (resource == null) {
            return Collections.emptyList();
        }

        JsonReader jsonReader = Optional.of(resource)
                .map(res -> {
                    try {
                        return new JsonReader(res, extractUniqueKeys(res));
                    } catch (IOException e) {
                        log.error("---- Failed to read JSON file", e);
                        return null;
                    }
                })
                .orElseThrow(() -> new RuntimeException("---- Failed to create JsonReader"));

        Map<String, Object> baseMetadata = Optional.ofNullable(resource.getFilename())
                .map(filename -> {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("file_name", filename);
                    return meta;
                })
                .orElse(new HashMap<>());

        return Optional.ofNullable(jsonReader.get())
                .map(docs -> docs.stream()
                        .filter(Objects::nonNull)
                        .map(document -> enrichDocument(document, baseMetadata))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private Document enrichDocument(Document document, Map<String, Object> baseMetadata) {
        if (document == null) {
            return null;
        }

        return Optional.of(document.getMetadata())
                .map(documentMetadata -> documentMetadata.entrySet().stream()
                        .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (v1, v2) -> v1,
                                () -> new HashMap<>(baseMetadata)
                        )))
                .map(enrichedMetadata -> {
                    try {
                        return new Document(
                                Optional.ofNullable(document.getText()).orElse(""),
                                enrichedMetadata
                        );
                    } catch (IllegalArgumentException e) {
                        log.error("---- Failed to create document with metadata", e);
                        return null;
                    }
                })
                .orElse(null);
    }

    private String[] extractUniqueKeys(Resource resource) throws IOException {
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
        Set<String> uniqueKeys = new HashSet<>();
        extractKeys(rootNode, "", uniqueKeys);
        return uniqueKeys.toArray(new String[0]);
    }

    private void extractKeys(JsonNode jsonNode, String currentPath, Set<String> keys) {
        if (jsonNode == null || jsonNode.isNull()) {
            return;
        }

        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (!value.isNull()) {
                    String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                    // Only add the key if it has a non-null value
                    if (isValidValue(value)) {
                        keys.add(newPath);
                    }
                    extractKeys(value, newPath, keys);
                }
            });
        } else if (jsonNode.isArray()) {
            // Only process array if it's not empty
            if (!jsonNode.isEmpty()) {
                for (int i = 0; i < jsonNode.size(); i++) {
                    JsonNode element = jsonNode.get(i);
                    if (!element.isNull()) {
                        extractKeys(element, currentPath + "[" + i + "]", keys);
                    }
                }
            }
        }
    }

    private boolean isValidValue(JsonNode node) {
        if (node.isNull()) {
            return false;
        }
        if (node.isTextual()) {
            return !node.asText().isEmpty();
        }
        if (node.isArray()) {
            return !node.isEmpty();
        }
        if (node.isObject()) {
            return !node.isEmpty();
        }
        // For numbers, booleans, etc.
        return true;
    }

    protected List<Document> loadText(String fileName, Resource resource) {
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("file_name", fileName);
        return textReader.read();
    }

    protected List<Document> loadPdf(Resource resource) {
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());
        return pdfReader.read();
    }

    protected List<Document> loadMarkdown(String fileName, Resource resource) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withAdditionalMetadata("file_name", fileName)
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        return reader.get();
    }
}
