package com.rsm.mock2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStudentRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotNull(message = "Grade cannot be null")
    Integer grade
) {}
