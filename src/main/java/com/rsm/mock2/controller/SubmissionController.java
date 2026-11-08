package com.rsm.mock2.controller;

import com.rsm.mock2.dto.CreateSubmissionRequest;
import com.rsm.mock2.dto.SubmissionResponse;
import com.rsm.mock2.service.GradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final GradingService gradingService;

    public SubmissionController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submitAnswer(@Valid @RequestBody CreateSubmissionRequest req) {
        SubmissionResponse response = gradingService.submitAnswer(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
