package org.cftoolsuite.service.crawl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cftoolsuite.domain.crawl.CrawlCompletedEvent;
import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

@ExtendWith(MockitoExtension.class)
class CustomWebCrawlerTest {

    private static final String ROOT_DOMAIN = "https://example.com";

    @TempDir
    Path tempDir;

    @Mock
    private ApplicationEventPublisher publisher;

    private CustomWebCrawler crawler;

    @Nested
    @DisplayName("shouldVisit tests")
    class ShouldVisitTests {

        @BeforeEach
        void setUp() {
            CrawlRequest request = new CrawlRequest(
                    ROOT_DOMAIN,
                    new String[] { ROOT_DOMAIN },
                    tempDir.toString(),
                    null,
                    null);
            crawler = new CustomWebCrawler(request, publisher);
        }

        @Test
        @DisplayName("Should reject URLs from different domains")
        void shouldRejectDifferentDomain() {
            Page referringPage = mock(Page.class);
            WebURL webURL = mock(WebURL.class);
            when(webURL.getURL()).thenReturn("https://different-domain.com/page.html");

            assertThat(crawler.shouldVisit(referringPage, webURL)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://example.com/path/page",
                "https://example.com/api/endpoint"
        })
        @DisplayName("Should accept URLs without extension")
        void shouldAcceptUrlsWithoutExtension(String url) {
            Page referringPage = mock(Page.class);
            WebURL webURL = mock(WebURL.class);
            when(webURL.getURL()).thenReturn(url);

            assertThat(crawler.shouldVisit(referringPage, webURL)).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "https://example.com/page.html,true",
                "https://example.com/data.json,false",
                "https://example.com/doc.xyz,false"
        })
        @DisplayName("Should correctly handle URLs with supported and unsupported extensions")
        void shouldHandleExtensions(String url, boolean expected) {
            Page referringPage = mock(Page.class);
            WebURL webURL = mock(WebURL.class);
            when(webURL.getURL()).thenReturn(url);

            assertThat(crawler.shouldVisit(referringPage, webURL)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("visit tests")
    class VisitTests {

        @BeforeEach
        void setUp() {
            CrawlRequest request = new CrawlRequest(
                    ROOT_DOMAIN,
                    new String[] { ROOT_DOMAIN },
                    tempDir.toString(),
                    null,
                    null);
            crawler = new CustomWebCrawler(request, publisher);
        }

        @Test
        @DisplayName("Should process HTML page correctly")
        void shouldProcessHtmlPage() throws IOException {
            // Arrange
            Page page = mock(Page.class);
            WebURL webURL = mock(WebURL.class);
            HtmlParseData parseData = mock(HtmlParseData.class);
            String html = "<html><body>Test</body></html>";
            String url = "https://example.com/test/page.html";

            when(page.getWebURL()).thenReturn(webURL);
            when(webURL.getURL()).thenReturn(url);
            when(page.getParseData()).thenReturn(parseData);
            when(page.getContentType()).thenReturn("text/html");
            when(parseData.getHtml()).thenReturn(html);
            //when(page.getContentData()).thenReturn(html.getBytes(StandardCharsets.UTF_8));

            // Create parent directory if it doesn't exist
            Files.createDirectories(tempDir);

            // Act
            crawler.visit(page);

            // Assert
            ArgumentCaptor<CrawlCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CrawlCompletedEvent.class);
            verify(publisher).publishEvent(eventCaptor.capture());

            CrawlCompletedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent).isNotNull();
            Path filePath = capturedEvent.getFilePath();
            assertThat(filePath).isNotNull();
            assertThat(filePath.toString()).endsWith(".html");

            // Verify the file exists and contains the correct content
            assertThat(Files.exists(filePath))
                    .withFailMessage("Expected file to exist at: " + filePath)
                    .isTrue();

            String savedContent = Files.readString(filePath);
            assertThat(savedContent)
                    .withFailMessage("Expected file to contain HTML content but got: " + savedContent)
                    .isEqualTo(html);
        }

        @Test
        @DisplayName("Should handle unsupported content type")
        void shouldHandleUnsupportedContentType() {
            // Arrange
            Page page = mock(Page.class);
            WebURL webURL = mock(WebURL.class);

            when(page.getWebURL()).thenReturn(webURL);
            when(webURL.getURL()).thenReturn("https://example.com/file.xyz");
            when(page.getContentType()).thenReturn("application/xyz");

            // Act
            crawler.visit(page);

            // Assert
            verify(publisher, never()).publishEvent(any(CrawlCompletedEvent.class));
        }
    }
}