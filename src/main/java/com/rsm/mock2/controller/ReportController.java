package com.rsm.mock2.controller;

import com.rsm.mock2.dto.ConceptMasteryResponse;
import com.rsm.mock2.dto.ParentReportResponse;
import com.rsm.mock2.service.MasteryReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReportController {

    private final MasteryReportService masteryReportService;

    public ReportController(MasteryReportService masteryReportService) {
        this.masteryReportService = masteryReportService;
    }

    @GetMapping("/students/{id}/mastery")
    public ResponseEntity<List<ConceptMasteryResponse>> getStudentMastery(@PathVariable("id") Long id) {
        List<ConceptMasteryResponse> mastery = masteryReportService.getStudentMastery(id);
        return ResponseEntity.ok(mastery);
    }

    @GetMapping("/students/{id}/report")
    public ResponseEntity<ParentReportResponse> getParentReport(@PathVariable("id") Long id) {
        ParentReportResponse report = masteryReportService.getParentReport(id);
        return ResponseEntity.ok(report);
    }
}
