package org.cftoolsuite.controller;

import org.cftoolsuite.domain.chat.AudioResponse;
import org.cftoolsuite.service.chat.ConverseService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Profile({"openai"})
@RestController
public class ConverseController {

    private final ConverseService converseService;

    public ConverseController(ConverseService converseService) {
        this.converseService = converseService;
    }

    @PostMapping("/api/converse")
    public ResponseEntity<AudioResponse> handleAudioUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(converseService.respondToAudioRequest(file));
    }
}
