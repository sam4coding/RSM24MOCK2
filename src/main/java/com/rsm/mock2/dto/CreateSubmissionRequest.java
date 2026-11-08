package com.rsm.mock2.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSubmissionRequest(
    @NotNull(message = "Student ID cannot be null")
    Long studentId,

    @NotNull(message = "Problem ID cannot be null")
    Long problemId,

    @NotNull(message = "Answer cannot be null")
    String answer
) {}
