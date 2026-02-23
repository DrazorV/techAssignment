package com.epanos.techassignment.models.dto;

import com.epanos.techassignment.models.enums.Sport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Response payload representing a Match")
public class MatchResponse {

    @Schema(description = "Match unique identifier", example = "1")
    private Long id;

    @Schema(description = "Match description", example = "OSFP-PAO")
    private String description;

    @Schema(description = "Match date", example = "2021-03-31", type = "string", format = "date")
    private LocalDate matchDate;

    @Schema(description = "Match time", example = "12:00", type = "string", format = "time")
    private LocalTime matchTime;

    @Schema(description = "Home team", example = "OSFP")
    private String teamA;

    @Schema(description = "Away team", example = "PAO")
    private String teamB;

    @Schema(description = "Sport type (1 = Football, 2 = Basketball)", example = "1")
    private Sport sport;

    @Schema(description = "List of match odds")
    private List<MatchOddsResponse> odds;
}