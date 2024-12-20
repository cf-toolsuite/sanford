package org.cftoolsuite.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DocumentSummarizationService {

    private final DocumentSearchService documentSearchService;
    private final ChatClient chatClient;

    public DocumentSummarizationService(DocumentSearchService documentSearchService, ChatModel model) {
        this.documentSearchService = documentSearchService;
        this.chatClient =
            ChatClient
                .builder(model)
                .defaultSystem("""
                    Summarize the following text into a concise paragraph that captures the main points and essential details without losing important information.
                    The summary should be as short as possible while remaining clear and informative.
                    Use bullet points or numbered lists to organize the information if it helps to clarify the meaning.
                    Focus on the key facts, events, and conclusions.
                    Avoid including minor details or examples unless they are crucial for understanding the main ideas.
                    """
                )
                .build();
    }

    // FIXME this is a naive summarization implementation which takes all the content from all document fragments and concatentates them together
    // TODO Optimize the summarization algorithm using chunking and recursive techniques
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
