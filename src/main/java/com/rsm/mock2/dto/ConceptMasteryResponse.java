package com.rsm.mock2.dto;

public record ConceptMasteryResponse(
    String concept,
    long attempts,
    long correct,
    double mastery
) {}
