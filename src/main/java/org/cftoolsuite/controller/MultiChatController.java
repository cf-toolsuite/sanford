package org.cftoolsuite.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.cftoolsuite.domain.chat.Inquiry;
import org.cftoolsuite.domain.chat.MultiChatResponse;
import org.cftoolsuite.service.chat.MultiChatService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile({"alting"})
@RestController
public class MultiChatController {

    private final MultiChatService multiChatService;

    public MultiChatController(MultiChatService multiChatService) {
        this.multiChatService = multiChatService;
    }

    @PostMapping("/api/multichat")
    public ResponseEntity<List<MultiChatResponse>> askQuestion(@RequestBody Inquiry inquiry) {
        if (CollectionUtils.isNotEmpty(inquiry.filter())) {
            return ResponseEntity.ok(multiChatService.askQuestion(inquiry.question(), inquiry.filter()));
        } else {
            return ResponseEntity.ok(multiChatService.askQuestion(inquiry.question()));
        }
    }
}
