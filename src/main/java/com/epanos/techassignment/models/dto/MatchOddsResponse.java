package com.epanos.techassignment.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Response payload representing Match Odds")
public class MatchOddsResponse {

    @Schema(description = "Odds unique identifier", example = "1")
    private Long id;

    @Schema(description = "Associated match ID", example = "1")
    private Long matchId;

    @Schema(description = "Specifier of the odd", example = "X")
    private String specifier;

    @Schema(description = "Odd value", example = "1.50")
    private BigDecimal odd;
}