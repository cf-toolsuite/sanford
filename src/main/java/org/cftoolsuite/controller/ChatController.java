package org.cftoolsuite.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.cftoolsuite.domain.chat.Inquiry;
import org.cftoolsuite.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<String> chat(@RequestBody Inquiry inquiry) {
        if (CollectionUtils.isNotEmpty(inquiry.filter())) {
            return ResponseEntity.ok(chatService.respondToQuestion(inquiry.question(), inquiry.filter()));
        } else {
            return ResponseEntity.ok(chatService.respondToQuestion(inquiry.question()));
        }
    }

    @PostMapping("/api/stream/chat")
    public ResponseEntity<Flux<String>> streamChat(@RequestBody Inquiry inquiry) {
        if (CollectionUtils.isNotEmpty(inquiry.filter())) {
            return ResponseEntity.ok(chatService.streamResponseToQuestion(inquiry.question(), inquiry.filter()));
        } else {
            return ResponseEntity.ok(chatService.streamResponseToQuestion(inquiry.question()));
        }
    }
}
