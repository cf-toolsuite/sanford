package org.cftoolsuite.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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

    public void ingest(MultipartFile file, FileMetadata fileMetadata) {
        log.info("-- Ingesting file: {}", fileMetadata.fileName());
        List<Document> documents = null;
        switch (fileMetadata.fileExtension().toLowerCase()) {
            case "md":
                documents = loadMarkdown(fileMetadata.fileName(), file.getResource());
                break;
            case "pdf":
                documents = loadPdf(file.getResource());
                break;
            case "log":
                documents = loadText(fileMetadata.fileName(), file.getResource());
                break;
            case "txt":
                documents = loadText(fileMetadata.fileName(), file.getResource());
                break;
            case "csv":
                documents = loadText(fileMetadata.fileName(), file.getResource());
                break;
            case "tsv":
                documents = loadText(fileMetadata.fileName(), file.getResource());
                break;
            case "json":
                documents = loadJson(file.getResource());
            case "xml":
                documents = loadXml(file.getResource());
                break;
            case "html":
                documents = loadTika(file.getResource());
                break;
            case "htm":
                documents = loadTika(file.getResource());
                break;
            case "doc":
                documents = loadTika(file.getResource());
                break;
            case "docx":
                documents = loadTika(file.getResource());
                break;
            case "ppt":
                documents = loadTika(file.getResource());
                break;
            case "pptx":
                documents = loadTika(file.getResource());
                break;
            default:
                throw new IllegalArgumentException("Filename [" + fileMetadata.fileName() + "] contains an unsupported file extension [" + fileMetadata.fileExtension() + "]");
        }

        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
        List<Document> keywordEnrichedDocuments = enricher.apply(documents);

        TokenTextSplitter splitter = new TokenTextSplitter();
        store.accept(splitter.apply(keywordEnrichedDocuments));
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
            enrichedDocuments.add(new Document(document.getContent(), customMetadata));
        }
        return enrichedDocuments;
    }

    protected List<Document> loadJson(Resource resource) {
        JsonReader jsonReader;
        try {
            jsonReader = new JsonReader(resource, extractUniqueKeys(resource));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file", e);
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file_name", resource.getFilename());
        List<Document> documents = jsonReader.get();
        List<Document> enrichedDocuments = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> customMetadata = new HashMap<>(metadata);
            customMetadata.putAll(document.getMetadata());
            enrichedDocuments.add(new Document(document.getContent(), customMetadata));
        }
        return enrichedDocuments;
    }

    private String[] extractUniqueKeys(Resource resource) throws IOException {
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
        Set<String> uniqueKeys = new HashSet<>();
        extractKeys(rootNode, "", uniqueKeys);
        return uniqueKeys.toArray(new String[0]);
    }

    private void extractKeys(JsonNode jsonNode, String currentPath, Set<String> keys) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                keys.add(newPath);
                extractKeys(entry.getValue(), newPath, keys);
            });
        } else if (jsonNode.isArray()) {
            for (int i = 0; i < jsonNode.size(); i++) {
                extractKeys(jsonNode.get(i), currentPath + "[" + i + "]", keys);
            }
        }
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