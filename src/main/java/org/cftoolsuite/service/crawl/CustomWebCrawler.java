package org.cftoolsuite.service.crawl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.crawl.CrawlCompletedEvent;
import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class CustomWebCrawler extends WebCrawler {
    private static final Logger log = LoggerFactory.getLogger(CustomWebCrawler.class);

    private final ContentTypeHandler contentTypeHandler;
    private final String rootDomain;
    private final String storageFolder;
    private final ApplicationEventPublisher publisher;
    private final Pattern includesFilter;

    public CustomWebCrawler(CrawlRequest crawlRequest, AppProperties appProperties,
            ApplicationEventPublisher publisher) {
        this.rootDomain = crawlRequest.rootDomain();
        this.storageFolder = crawlRequest.storageFolder();
        this.publisher = publisher;
        this.contentTypeHandler = new ContentTypeHandler(appProperties.supportedContentTypes());
        this.includesFilter = Pattern.compile(
                StringUtils.isNotBlank(crawlRequest.includesRegexFilter())
                        ? crawlRequest.includesRegexFilter()
                        : "");
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean shouldVisit = false;
        if (href.startsWith(rootDomain)) {
            String extension = FilenameUtils.getExtension(href);

            if (StringUtils.isBlank(extension)) {
                shouldVisit = true;
            } else if (StringUtils.isNotBlank(includesFilter.pattern())) {
                shouldVisit = includesFilter.matcher(href).matches();
            } else {
                shouldVisit = contentTypeHandler.isSupportedExtension(extension);
            }
        }
        String action = shouldVisit ? "will": "will NOT";
        log.debug("{} {} be visited", href, action);
        return shouldVisit;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        log.debug("Crawling URL: {}", url);

        try {
            Optional<CrawlResult> result = processPage(page);
            if (result.isPresent()) {
                CrawlResult crawlResult = result.get();
                String fileName = FileNameGenerator.generate(url, crawlResult.extension());
                Path filePath = Paths.get(storageFolder, fileName);

                // Create parent directories if they don't exist
                Files.createDirectories(filePath.getParent());

                // Write the content to file
                Files.write(filePath, crawlResult.content().getBytes(StandardCharsets.UTF_8));

                // Publish the event
                publisher.publishEvent(new CrawlCompletedEvent(this).filePath(filePath));

                log.debug("Successfully processed and saved content from URL: {} to file: {}", url, filePath);
            } else {
                log.warn("Unsupported content type for URL: {}", url);
            }
        } catch (IOException e) {
            log.error("Error processing URL: {}", url, e);
        }
    }

    private Optional<CrawlResult> processPage(Page page) {
        String contentType = page.getContentType();
        if (StringUtils.isBlank(contentType)) {
            log.debug("Content type is blank for page");
            return Optional.empty();
        }

        // Clean up content type (remove charset, etc.)
        contentType = contentType.split(";")[0].trim().toLowerCase();
        log.debug("Processing page with content type: {}", contentType);

        // Check if content type is supported
        if (!contentTypeHandler.isSupportedContentType(contentType)) {
            log.debug("Unsupported content type: {}", contentType);
            return Optional.empty();
        }

        String url = page.getWebURL().getURL().toLowerCase();
        String extension = FilenameUtils.getExtension(url);

        if (StringUtils.isBlank(extension)) {
            // Get the set of extensions for the content type
            Set<String> extensions = contentTypeHandler.getExtensionsForContentType(contentType);
            if (extensions.isEmpty()) {
                log.debug("No extensions found for content type: {}", contentType);
                return Optional.empty();
            }
            extension = extensions.iterator().next();
        }

        // Special handling for HTML content
        if (page.getParseData() instanceof HtmlParseData htmlParseData) {
            return Optional.of(new CrawlResult(
                    htmlParseData.getHtml(),
                    extension));
        }

        // Handle other content types
        return Optional.of(new CrawlResult(
                new String(page.getContentData(), StandardCharsets.UTF_8),
                extension));
    }

}

class ContentTypeHandler {
    private final Map<String, String> extensionToContentType;
    private final Map<String, Set<String>> contentTypeToExtensions;

    public ContentTypeHandler(Map<String, String> supportedContentTypes) {
        this.extensionToContentType = new HashMap<>(supportedContentTypes);

        this.contentTypeToExtensions = supportedContentTypes.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().toLowerCase(),
                        Collectors.mapping(
                                entry -> entry.getKey().toLowerCase(),
                                Collectors.toSet())));
    }

    public boolean isSupportedExtension(String extension) {
        return extensionToContentType.containsKey(extension.toLowerCase());
    }

    public boolean isSupportedContentType(String contentType) {
        return contentTypeToExtensions.containsKey(contentType.toLowerCase());
    }

    public Set<String> getExtensionsForContentType(String contentType) {
        return contentTypeToExtensions.getOrDefault(contentType.toLowerCase(), Collections.emptySet());
    }

    public Optional<String> getContentTypeForExtension(String extension) {
        return Optional.ofNullable(extensionToContentType.get(extension.toLowerCase()));
    }
}

record CrawlResult(String content, String extension) {
}

class FileNameGenerator {
    public static String generate(String url, String extension) {
        String baseName = extractBaseName(url);
        return baseName + "." + extension;
    }

    private static String extractBaseName(String url) {
        // Remove protocol and domain
        String path = StringUtils.substringAfter(url, "://");
        path = StringUtils.substringAfter(path, "/");

        // Remove query parameters and fragments
        path = StringUtils.substringBefore(path, "?");
        path = StringUtils.substringBefore(path, "#");

        // Remove file extension if present
        path = FilenameUtils.removeExtension(path);

        // Replace remaining slashes with hyphens and clean up
        return path.replace("/", "-")
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }
}
