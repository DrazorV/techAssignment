package com.epanos.techassignment.controllers;

import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.services.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "CRUD operations for managing Matches and their Odds.")
public class MatchController {

    private final MatchService matchService;

    @Operation(
            summary = "Create match",
            description = "Creates a single Match. Optionally accepts an odds array that will be created together with the match.",
            operationId = "createMatch"
    )
    @ApiResponse(responseCode = "201", description = "Match created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse create(@Valid @RequestBody MatchRequest req) {
        return matchService.create(req);
    }

    @Operation(
            summary = "Create matches in bulk",
            description = "Creates multiple Matches in a single request. Each match may optionally include odds. The operation is transactional.",
            operationId = "createMatchesBulk"
    )
    @ApiResponse(responseCode = "201", description = "Matches created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchResponse> createBulk(@RequestBody @Valid List<@Valid MatchRequest> reqs) {
        return matchService.createBulk(reqs);
    }

    @Operation(
            summary = "Get match by id",
            description = "Returns a single Match by id. This endpoint always includes odds (if any exist).",
            operationId = "getMatchById"
    )
    @ApiResponse(responseCode = "200", description = "Match found")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return matchService.get(id);
    }

    @Operation(
            summary = "List matches",
            description = "Returns all matches. By default odds are not included. Use ?includeOdds=true to include odds in the response.",
            operationId = "listMatches"
    )
    @ApiResponse(responseCode = "200", description = "Matches returned successfully")
    @GetMapping
    public List<MatchResponse> list(@RequestParam(defaultValue = "false") boolean includeOdds) {
        return matchService.list(includeOdds);
    }

    @Operation(
            summary = "Update match by id",
            description = "Replaces match fields with the provided payload. If odds are provided, they replace the existing odds collection. If odds are omitted, existing odds are left unchanged.",
            operationId = "updateMatchById"
    )
    @ApiResponse(responseCode = "200", description = "Match updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @PutMapping("/{id}")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest req) {
        return matchService.update(id, req);
    }

    @Operation(
            summary = "Delete match by id",
            description = "Deletes a match by id. Associated odds are deleted as well.",
            operationId = "deleteMatchById"
    )
    @ApiResponse(responseCode = "204", description = "Match deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        matchService.delete(id);
    }
}