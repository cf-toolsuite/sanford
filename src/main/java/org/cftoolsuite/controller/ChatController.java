package org.cftoolsuite.controller;

import org.cftoolsuite.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/api/chat")
    public ResponseEntity<String> chat(@RequestParam("q") String message) {
        String response = chatService.askQuestion(message);
        return ResponseEntity.ok(response);
    }
}
