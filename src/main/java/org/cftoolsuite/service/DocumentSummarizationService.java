package org.cftoolsuite.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;


@Service
// FIXME this is a naive summarization implementation which takes all the content from all document fragments and concatentates them together
// TODO Optimize the summarization algorithm using chunking and recursive techniques
public class DocumentSummarizationService {

    private final DocumentSearchService documentSearchService;
    private final ChatClient chatClient;

    public DocumentSummarizationService(DocumentSearchService documentSearchService, ChatClient chatClient) {
        this.documentSearchService = documentSearchService;
        this.chatClient = chatClient;
    }

    @Deprecated
    public String summarize(String fileName) {
        List<Document> candidates = documentSearchService.search(fileName);
        String content = candidates.stream().map(d -> d.getContent()).reduce("", (a, b) -> a + b);
        return
            chatClient
                .prompt()
                .user(String.format("Text to summarize: %s", content))
                .call()
                .content();
    }

    public Flux<String> summarizeAsStream(String fileName) {
        List<Document> candidates = documentSearchService.search(fileName);
        String content = candidates.stream().map(d -> d.getContent()).reduce("", (a, b) -> a + b);
        return
            chatClient
                .prompt()
                .user(String.format("Text to summarize: %s", content))
                .stream()
                .content();
    }

}
