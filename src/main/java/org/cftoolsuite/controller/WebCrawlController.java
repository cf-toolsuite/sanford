package org.cftoolsuite.controller;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;

import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.cftoolsuite.domain.crawl.CrawlResponse;
import org.cftoolsuite.service.crawl.CustomWebCrawler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import crawlercommons.filters.basic.BasicURLNormalizer;
import de.hshn.mi.crawler4j.frontier.URLFrontierConfiguration;
import de.hshn.mi.crawler4j.url.URLFrontierWebURLFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@RestController
public class WebCrawlController {

    private static final AtomicInteger crawlId = new AtomicInteger(0);

    private final ApplicationEventPublisher publisher;

    public WebCrawlController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/crawl")
    public ResponseEntity<CrawlResponse> startCrawl(@RequestBody CrawlRequest crawlRequest) {
        String id = String.valueOf(crawlId.incrementAndGet());
        String crawlStorageFolder = crawlRequest.storageFolder() + "/" + id;

        try {
            CrawlConfig config = new CrawlConfig();
            config.setCrawlStorageFolder(crawlStorageFolder);
            config.setResumableCrawling(true);

            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            PageFetcher pageFetcher = new PageFetcher(config, normalizer);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer =
                new RobotstxtServer(robotstxtConfig, pageFetcher, new URLFrontierWebURLFactory());
            FrontierConfiguration frontierConfiguration =
                new URLFrontierConfiguration(config, 10, crawlRequest.rootDomain(), 443);

            try {
                CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer,
                        frontierConfiguration);

                for (String seed : crawlRequest.seeds()) {
                    controller.addSeed(seed);
                }

                CrawlController.WebCrawlerFactory<CustomWebCrawler> factory = () -> new CustomWebCrawler(crawlRequest, publisher);

                controller.start(factory, crawlRequest.numberOfCrawlers());

                CrawlResponse response = new CrawlResponse(id, crawlStorageFolder, "Accepted");
                return ResponseEntity.accepted().body(response);
            } catch (Exception e) {
                return createErrorResponse(id, crawlStorageFolder, HttpStatus.INTERNAL_SERVER_ERROR, "Runtime Error",
                        e);
            }

        } catch (IllegalArgumentException e) {
            return createErrorResponse(id, crawlStorageFolder, HttpStatus.BAD_REQUEST, "Invalid Argument", e);
        } catch (SecurityException e) {
            return createErrorResponse(id, crawlStorageFolder, HttpStatus.FORBIDDEN, "Security Error", e);
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private ResponseEntity<CrawlResponse> createErrorResponse(String id, String storageFolder, HttpStatus status,
            String errorType, Exception e) {
        String result = String.format("HTTP %d: %s\n%s", status.value(), errorType, e.getMessage());
        CrawlResponse response = new CrawlResponse(id, storageFolder, result);
        return ResponseEntity.status(status).body(response);
    }
}
