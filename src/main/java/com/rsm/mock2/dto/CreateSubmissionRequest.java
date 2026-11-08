package com.rsm.mock2.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSubmissionRequest(
    @NotNull Long studentId,
    @NotNull Long problemId,
    @NotNull String answer
) {}
