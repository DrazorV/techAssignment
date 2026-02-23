package com.epanos.techassignment.controllers;

import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.services.MatchOddsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/odds")
@RequiredArgsConstructor
@Tag(name = "Match Odds",
        description = "Endpoints for managing odds associated with a specific match. Each odd is uniquely identified per match by its specifier."
)
public class MatchOddsController {

    private final MatchOddsService matchOddsService;

    @Operation(
            summary = "Create match odd",
            description = "Creates a new odd for the specified match. Each match cannot have duplicate specifiers.",
            operationId = "createMatchOdd"
    )
    @ApiResponse(responseCode = "201", description = "Match odd created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchOddsResponse create(@PathVariable Long matchId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.create(matchId, request);
    }

    @Operation(
            summary = "Create match odds in bulk",
            description = "Creates multiple odds for the specified match in a single request. " +
                    "The operation is transactional (all-or-nothing). Specifiers must be unique per match.",
            operationId = "createMatchOddsBulk"
    )
    @ApiResponse(responseCode = "201", description = "Match odds created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Duplicate specifier (payload or existing)", content = @Content)
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchOddsResponse> createBulk(
            @Parameter(description = "Match id", example = "1", required = true)
            @PathVariable Long matchId,
            @Valid @RequestBody List<MatchOddsRequest> requests
    ) {
        return matchOddsService.createBulk(matchId, requests);
    }

    @Operation(
            summary = "Update match odd",
            description = "Updates an existing odd for the specified match. Performs a full replacement of the odd fields.",
            operationId = "updateMatchOdd"
    )
    @ApiResponse(responseCode = "200", description = "Match odd updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @PutMapping("/{oddId}")
    public MatchOddsResponse update(@PathVariable Long matchId, @PathVariable Long oddId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.update(matchId, oddId, request);
    }

    @Operation(
            summary = "Get match odd by id",
            description = "Returns a specific odd belonging to the specified match.",
            operationId = "getMatchOddById"
    )
    @ApiResponse(responseCode = "200", description = "Match odd returned successfully")
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @GetMapping("/{oddId}")
    public MatchOddsResponse get(
            @Parameter(description = "Match id", example = "1", required = true)
            @PathVariable Long matchId,
            @Parameter(description = "Odd id", example = "10", required = true)
            @PathVariable Long oddId) {
        return matchOddsService.get(matchId, oddId);
    }

    @Operation(
            summary = "List match odds",
            description = "Returns all odds for the specified match.",
            operationId = "listMatchOdds"
    )
    @ApiResponse(responseCode = "200", description = "Odds returned successfully")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @GetMapping
    public List<MatchOddsResponse> listByMatch(@Parameter(description = "Match id", example = "1", required = true) @PathVariable Long matchId) {
        return matchOddsService.listByMatch(matchId);
    }

    @Operation(
            summary = "Delete match odd",
            description = "Deletes an existing odd from the specified match.",
            operationId = "deleteMatchOdd"
    )
    @ApiResponse(responseCode = "204", description = "Match odd deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @DeleteMapping("/{oddId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long matchId, @PathVariable Long oddId) {
        matchOddsService.delete(matchId, oddId);
    }

    @Operation(
            summary = "Delete all match odds",
            description = "Deletes all odds associated with the specified match.",
            operationId = "deleteAllMatchOdds"
    )
    @ApiResponse(responseCode = "204", description = "All match odds deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@Parameter(description = "Match id", example = "1", required = true) @PathVariable Long matchId) {
        matchOddsService.deleteAll(matchId);
    }
}
