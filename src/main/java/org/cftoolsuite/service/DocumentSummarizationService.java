package org.cftoolsuite.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;


@Service
public class DocumentSummarizationService {

    private final DocumentSearchService documentSearchService;
    private final ChatClient chatClient;

    public DocumentSummarizationService(DocumentSearchService documentSearchService, ChatClient chatClient) {
        this.documentSearchService = documentSearchService;
        this.chatClient = chatClient;
    }

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

}
