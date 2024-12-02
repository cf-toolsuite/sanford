package org.cftoolsuite.controller;

import java.util.List;

import org.cftoolsuite.domain.fetch.FetchRequest;
import org.cftoolsuite.domain.fetch.FetchResponse;
import org.cftoolsuite.domain.fetch.FetchResult;
import org.cftoolsuite.service.fetch.FetchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FetchController {
    private final FetchService fetchService;

    public FetchController(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @PostMapping("/api/fetch")
    public ResponseEntity<FetchResponse> fetchUrls(@RequestBody FetchRequest request) {
        List<FetchResult> results = fetchService.fetchAndSave(request.urls());
        return ResponseEntity.ok(FetchResponse.from(results));
    }
}
