package com.rsm.mock2.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProblemRequest(
    @NotBlank String concept,
    @NotBlank String question,
    @NotBlank String correctAnswer,
    Double tolerance
) {}
