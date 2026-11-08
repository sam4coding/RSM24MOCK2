package com.rsm.mock2.dto;

import java.time.Instant;

public record SubmissionResponse(
    Long id,
    Long studentId,
    Long problemId,
    String answer,
    boolean correct,
    Double score,
    String correctAnswer,
    Instant submittedAt
) {}
