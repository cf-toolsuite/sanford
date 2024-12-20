package org.cftoolsuite.controller;

import org.cftoolsuite.domain.chat.AudioResponse;
import org.cftoolsuite.service.chat.ConverseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Profile({"openai"})
@RestController
public class ConverseController {

    private static Logger log = LoggerFactory.getLogger(ConverseController.class);

    private final ConverseService converseService;

    public ConverseController(ConverseService converseService) {
        this.converseService = converseService;
    }

    @PostMapping(value = "/api/converse", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<AudioResponse> handleAudioUpload(@RequestBody byte[] audioBytes) {
        return ResponseEntity.ok(converseService.respondToAudioRequest(audioBytes));
    }
}
