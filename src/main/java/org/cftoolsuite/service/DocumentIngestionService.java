package org.cftoolsuite.service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cftoolsuite.domain.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.yaml.snakeyaml.Yaml;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final ChatModel chatModel;
    private final VectorStore store;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    private final Yaml yamlDumper;

    public DocumentIngestionService(ChatModel chatModel, VectorStore store, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.store = store;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
        this.yamlDumper = new Yaml();
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
                documents = loadJson(fileName, resource);
                break;
            case "xml":
                documents = loadXml(fileName, resource);
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

    private List<Document> loadXml(String fileName, Resource resource) {
        try {
            Object xmlObject = xmlMapper.readValue(resource.getInputStream(), Object.class);
            StringWriter writer = new StringWriter();
            yamlDumper.dump(xmlObject, writer);
            String content = writer.toString();
            return loadText(fileName, new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to either read XML or write XML content as YAML", e);
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
        log.trace("{}", enrichedDocuments);
        return enrichedDocuments;
    }

    protected List<Document> loadJson(String fileName, Resource resource) {
        try {
            Object jsonObject = objectMapper.readValue(resource.getInputStream(), Object.class);
            StringWriter writer = new StringWriter();
            yamlDumper.dump(jsonObject, writer);
            String content = writer.toString();
            return loadText(fileName, new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to either read JSON or write JSON content as YAML", e);
        }
    }

    protected List<Document> loadText(String fileName, Resource resource) {
        TextReader textReader = new TextReader(resource);
		textReader.getCustomMetadata().put("file_name", fileName);
		List<Document> result = textReader.read();
        log.trace("{}", result);
        return result;
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
        List<Document> result = pdfReader.read();
        log.trace("{}", result);
	    return result;
    }

    protected List<Document> loadMarkdown(String fileName, Resource resource) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
            .withHorizontalRuleCreateDocument(true)
            .withAdditionalMetadata("file_name", fileName)
            .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        List<Document> result = reader.read();
        log.trace("{}", result);
        return result;
    }
}
