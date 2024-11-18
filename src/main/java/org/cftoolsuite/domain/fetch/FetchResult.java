package org.cftoolsuite.domain.fetch;

public record FetchResult(
    String url,
    String savedPath,
    boolean success,
    String error
) {
    public static FetchResult success(String url, String savedPath) {
        return new FetchResult(url, savedPath, true, null);
    }

    public static FetchResult failure(String url, String error) {
        return new FetchResult(url, null, false, error);
    }
}
