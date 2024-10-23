package org.cftoolsuite.service.crawl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.cftoolsuite.domain.crawl.CrawledMultipartFile;
import org.cftoolsuite.domain.crawl.CrawlCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

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

            try {
                String fileName = URLEncoder.encode(url, StandardCharsets.UTF_8.toString()) + ".html";
                Path filePath = Paths.get(storageFolder, fileName);
                Files.write(filePath, html.getBytes(StandardCharsets.UTF_8));
                publisher.publishEvent(new CrawlCompletedEvent(this).file(createMultipartFileFromPath(filePath)));
            } catch (IOException e) {
                log.error("Error ingesting file from URL: " + url, e);
            }
        }
    }

    private MultipartFile createMultipartFileFromPath(Path path) throws IOException {
        String name = path.getFileName().toString();
        String originalFileName = name;
        String contentType = Files.probeContentType(path);
        byte[] content = Files.readAllBytes(path);
        return new CrawledMultipartFile(name, originalFileName, contentType, content);
    }
}
