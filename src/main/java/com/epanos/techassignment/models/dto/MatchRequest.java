package com.epanos.techassignment.models.dto;

import com.epanos.techassignment.models.enums.Sport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "Request payload used for creating or updating a Match. All fields are required. For updates, this represents a full replacement (PUT semantics).")
public class MatchRequest {

    @NotBlank
    @Size(min = 1, max = 255, message = "Description must be between 1 and 255 characters")
    @Schema(description = "Human-readable description of the match", example = "OSFP-PAO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull
    @Schema(description = "Date of the match (ISO format yyyy-MM-dd)", example = "2021-03-31", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate matchDate;

    @NotNull
    @Schema(description = "Time of the match (ISO format HH:mm)", example = "12:00", type = "string", format = "time", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime matchTime;

    @NotBlank
    @Size(min = 1, max = 100, message = "Team name must be between 1 and 100 characters")
    @Schema(description = "Home team name", example = "OSFP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teamA;

    @NotBlank
    @Size(min = 1, max = 100, message = "Team name must be between 1 and 100 characters")
    @Schema(description = "Away team name", example = "PAO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teamB;

    @NotNull
    @Schema(description = "Sport type (1 = Football, 2 = Basketball)", example = "1", allowableValues = {"1", "2"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private Sport sport;

    @Valid
    @Schema(description = "Optional list of match odds. If present during update, existing odds will be replaced.", nullable = true)
    private List<MatchOddsRequest> odds;
}