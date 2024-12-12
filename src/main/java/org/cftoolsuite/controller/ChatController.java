package org.cftoolsuite.controller;

import org.apache.commons.collections.MapUtils;
import org.cftoolsuite.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/api/chat")
    public ResponseEntity<String> chat(
            @RequestParam("q") String message,
            @RequestParam(value = "f", required = false) Map<String, Object> filterMetadata
    ) {
        if (MapUtils.isNotEmpty(filterMetadata)) {
            return ResponseEntity.ok(chatService.respondToQuestion(message, filterMetadata));
        } else {
            return ResponseEntity.ok(chatService.respondToQuestion(message));
        }
    }

    @GetMapping("/api/stream/chat")
    public ResponseEntity<Flux<String>> streamChat(
            @RequestParam("q") String message,
            @RequestParam(value = "f", required = false) Map<String, Object> filterMetadata
    ) {
        if (MapUtils.isNotEmpty(filterMetadata)) {
            return ResponseEntity.ok(chatService.streamResponseToQuestion(message, filterMetadata));
        } else {
            return ResponseEntity.ok(chatService.streamResponseToQuestion(message));
        }
    }
}
