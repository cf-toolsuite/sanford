package org.cftoolsuite.service.fetch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.fetch.FetchCompletedEvent;
import org.cftoolsuite.domain.fetch.FetchResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class FetchService {

    private final HttpClient httpClient;
    private final AppProperties appProperties;
    private final ApplicationEventPublisher publisher;

    public FetchService(AppProperties appProperties, ApplicationEventPublisher publisher) {
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        this.appProperties = appProperties;
        this.publisher = publisher;
    }

    public List<FetchResult> fetchAndSave(Set<String> urls) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));
        String parentForStorageFolder = String.join(System.getProperty("file.separator"), System.getProperty("java.io.tmpdir"), "fetch");
        Path baseDir = Path.of(parentForStorageFolder, timestamp);
        List<FetchResult> results = new ArrayList<>();

        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory", e);
        }

        for (String url : urls) {
            try {
                results.add(fetchAndSaveUrl(url, baseDir));
            } catch (Exception e) {
                results.add(FetchResult.failure(url, e.getMessage()));
            }
        }

        publisher.publishEvent(new FetchCompletedEvent(this).results(results));
        return results;
    }

    private FetchResult fetchAndSaveUrl(String url, Path baseDir) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<byte[]> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofByteArray());

        String contentType = getBaseContentType(response);
        String extension = getExtensionForContentType(contentType)
            .orElseThrow(() -> new UnsupportedContentTypeException(
                "Unsupported content type: " + contentType));

        String filename = createFilename(url, extension);
        Path filePath = baseDir.resolve(filename);

        Files.write(filePath, response.body());
        return FetchResult.success(url, filePath.toString());
    }

    private String getBaseContentType(HttpResponse<?> response) {
        String contentType = response.headers()
            .firstValue("Content-Type")
            .orElse("application/octet-stream");
        // Extract base content type without charset or other parameters
        return contentType.split(";")[0].trim().toLowerCase();
    }

    private Optional<String> getExtensionForContentType(String contentType) {
        return appProperties.supportedContentTypes().entrySet().stream()
            .filter(entry -> entry.getValue().equalsIgnoreCase(contentType))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    private String createFilename(String url, String extension) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (path.isEmpty()) path = "/index";

            // Remove scheme, port, query parameters, and fragments
            String filename = (host + path)
                .replaceAll("[^a-zA-Z0-9/.]", "-")
                .replaceAll("/+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-", "")
                .replaceAll("-$", "");

            return filename + "." + extension;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create filename from URL: " + url, e);
        }
    }
}
