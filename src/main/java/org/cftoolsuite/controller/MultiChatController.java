package org.cftoolsuite.controller;

import java.util.List;

import org.cftoolsuite.domain.chat.MultiChatResponse;
import org.cftoolsuite.service.chat.MultiChatService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile({"alting"})
@RestController
public class MultiChatController {

    private final MultiChatService multiChatService;

    public MultiChatController(MultiChatService multiChatService) {
        this.multiChatService = multiChatService;
    }

    @GetMapping("/api/multichat")
    public ResponseEntity<List<MultiChatResponse>> askQuestion(@RequestParam("q") String message) {
        List<MultiChatResponse> response = multiChatService.askQuestion(message);
        return ResponseEntity.ok(response);
    }
}
