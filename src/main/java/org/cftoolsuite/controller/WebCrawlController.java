package org.cftoolsuite.controller;

import crawlercommons.filters.basic.BasicURLNormalizer;
import de.hshn.mi.crawler4j.frontier.HSQLDBFrontierConfiguration;
import de.hshn.mi.crawler4j.url.HSQLDBWebURLFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.cftoolsuite.domain.crawl.CrawlResponse;
import org.cftoolsuite.service.crawl.CustomWebCrawler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class WebCrawlController {

    private static final AtomicInteger crawlId = new AtomicInteger(0);

    private final ApplicationEventPublisher publisher;

    public WebCrawlController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/api/crawl")
    public ResponseEntity<CrawlResponse> startCrawl(@RequestBody CrawlRequest crawlRequest) {
        String id = String.valueOf(crawlId.incrementAndGet());
        String crawlStorageFolder = crawlRequest.storageFolder() + "/" + id;

        try {
            CrawlConfig config = new CrawlConfig();
            config.setRespectNoIndex(false);
            config.setRespectNoFollow(false);
            config.setCrawlStorageFolder(crawlStorageFolder);
            config.setResumableCrawling(true);
            config.setMaxDepthOfCrawling(crawlRequest.maxDepthOfCrawling());

            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            PageFetcher pageFetcher = new PageFetcher(config, normalizer);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer =
                new RobotstxtServer(robotstxtConfig, pageFetcher, new HSQLDBWebURLFactory());
            FrontierConfiguration frontierConfiguration =
                new HSQLDBFrontierConfiguration(config, 25);

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
