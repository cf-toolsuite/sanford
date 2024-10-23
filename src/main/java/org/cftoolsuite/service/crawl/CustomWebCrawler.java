package org.cftoolsuite.service.crawl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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

    private static Logger log = LoggerFactory.getLogger(CustomWebCrawler.class);

    private final Pattern INCLUDES_FILTER;
    private final String rootDomain;
    private final String storageFolder;
    private final ApplicationEventPublisher publisher;

    public CustomWebCrawler(CrawlRequest crawlRequest, ApplicationEventPublisher publisher) {
        this.INCLUDES_FILTER = Pattern.compile(crawlRequest.includesRegexFilter());
        this.rootDomain = crawlRequest.rootDomain();
        this.storageFolder = crawlRequest.storageFolder();
        this.publisher = publisher;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return INCLUDES_FILTER.matcher(href).matches() && href.startsWith(rootDomain);
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        log.debug("Crawling URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            log.debug("-- Text length: " + text.length());
            log.debug("-- HTML length: " + html.length());
            log.debug("-- Number of outgoing links: " + links.size());

            String fileName = "";
            try {
                fileName = extractFilename(url);
                Path filePath = Paths.get(storageFolder, fileName);
                Files.write(filePath, html.getBytes(StandardCharsets.UTF_8));
                publisher.publishEvent(new CrawlCompletedEvent(this).filePath(filePath));
            } catch (IOException e) {
                log.error("Error ingesting file " + fileName + " from URL: " + url, e);
            } catch (NullPointerException e) {
                log.error("Could not determine filename from URL: " + url, e);
            }
        }
    }

    private String extractFilename(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        // Remove any query parameters or fragments
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            url = url.substring(0, queryIndex);
        }

        int fragmentIndex = url.indexOf('#');
        if (fragmentIndex != -1) {
            url = url.substring(0, fragmentIndex);
        }

        // Extract the path after the domain
        int protocolIndex = url.indexOf("://");
        if (protocolIndex != -1) {
            url = url.substring(protocolIndex + 3);
        }

        int domainEndIndex = url.indexOf('/', protocolIndex + 3);
        if (domainEndIndex != -1) {
            url = url.substring(domainEndIndex + 1);
        }

        // Split the remaining path by '/'
        String[] parts = url.split("/");

        // Get the last non-empty part
        String lastPart = "";
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                lastPart = parts[i];
                break;
            }
        }

        // If no valid part found, return null
        if (lastPart.isEmpty()) {
            return null;
        }

        // Check if the last part already has a file extension
        if (lastPart.matches(".*\\.[^.]+")) {
            return lastPart;
        } else {
            // Append "-index.html" if there's no file extension
            return lastPart + "-index.html";
        }
    }
}
