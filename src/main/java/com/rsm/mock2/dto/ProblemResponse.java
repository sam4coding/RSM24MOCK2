package com.rsm.mock2.dto;

public record ProblemResponse(
    Long id,
    String concept,
    String question,
    String correctAnswer,
    Double tolerance
) {}
