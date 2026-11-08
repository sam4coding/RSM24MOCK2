package com.rsm.mock2.dto;

import java.time.Instant;
import java.util.List;

public record ParentReportResponse(
    Long studentId,
    String studentName,
    long totalSubmissions,
    Instant mostRecentSubmissionTime,
    String weakestConcept,
    List<ConceptMasteryResponse> masteryPerConcept
) {}
