package com.epanos.techassignment.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload used for creating or updating Match Odds. All fields are required. Updates follow full replacement (PUT semantics).")
public class MatchOddsRequest {

    @NotBlank
    @Size(min = 1, max = 16, message = "Specifier must be between 1 and 16 characters")
    @Schema(description = "Specifier of the odd (e.g. 1, X, 2)", example = "X", requiredMode = Schema.RequiredMode.REQUIRED)
    private String specifier;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Odd value must be a positive decimal number")
    @Schema(description = "Odd value (must be positive decimal number)", example = "1.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal odd;
}