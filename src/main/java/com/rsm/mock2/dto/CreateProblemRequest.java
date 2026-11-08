package com.rsm.mock2.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProblemRequest(
    @NotBlank(message = "Concept cannot be blank")
    String concept,

    @NotBlank(message = "Question cannot be blank")
    String question,

    @NotBlank(message = "Correct answer cannot be blank")
    String correctAnswer,

    Double tolerance
) {}
