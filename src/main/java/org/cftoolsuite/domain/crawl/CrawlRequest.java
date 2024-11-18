package org.cftoolsuite.domain.crawl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public record CrawlRequest(
    String rootDomain,
    String[] seeds,
    String storageFolder,
    Integer maxDepthOfCrawling,
    String includesRegexFilter,
    Integer numberOfCrawlers
) {
    public CrawlRequest {
        Assert.hasText(rootDomain, "A root domain must be specified!");
        Assert.isTrue(seeds != null && seeds.length >= 1, "At least one seed URL must be specified!");
        String parentForStorageFolder = String.join(System.getProperty("file.separator"), System.getProperty("java.io.tmpdir"), "crawler4j");
        if (StringUtils.isBlank(storageFolder)) {
            storageFolder = parentForStorageFolder;
        } else {
            storageFolder = String.join(System.getProperty("file.separator"), parentForStorageFolder, storageFolder);
        }
        if (maxDepthOfCrawling == null || maxDepthOfCrawling <= 0) {
            maxDepthOfCrawling = -1;
        }
        if (numberOfCrawlers == null || numberOfCrawlers <= 0) {
            numberOfCrawlers = 3;
        }
    }
}
