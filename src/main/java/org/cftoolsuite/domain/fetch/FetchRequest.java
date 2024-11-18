package org.cftoolsuite.domain.fetch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record FetchRequest(Set<String> urls) {
    public FetchRequest {
        if (urls != null) {
            validateUrls(urls);
        }
    }

    public static FetchRequest fromCommaSeparatedList(String urlList) {
        if (urlList == null || urlList.trim().isEmpty()) {
            throw new IllegalArgumentException("URL list cannot be empty");
        }
        Set<String> urls = Arrays.stream(urlList.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
        return new FetchRequest(urls);
    }

    private void validateUrls(Set<String> urls) {
        Set<String> invalidUrls = new HashSet<>();
        for (String url : urls) {
            try {
                new URI(url).toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                invalidUrls.add(url);
            }
        }
        if (!invalidUrls.isEmpty()) {
            throw new IllegalArgumentException("Invalid URLs detected: " + String.join(", ", invalidUrls));
        }
    }
}
