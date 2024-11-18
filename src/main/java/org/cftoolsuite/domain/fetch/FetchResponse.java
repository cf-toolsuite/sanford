package org.cftoolsuite.domain.fetch;

import java.util.List;

public record FetchResponse(
    List<FetchResult> results,
    int totalUrls,
    int successCount,
    int failureCount
) {
    public static FetchResponse from(List<FetchResult> results) {
        int successCount = (int) results.stream().filter(FetchResult::success).count();
        return new FetchResponse(
            results,
            results.size(),
            successCount,
            results.size() - successCount
        );
    }
}
