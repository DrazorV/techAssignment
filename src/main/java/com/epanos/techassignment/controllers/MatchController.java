package com.epanos.techassignment.controllers;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.services.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "CRUD operations for managing Matches and their Odds.")
public class MatchController {

    private final MatchService matchService;

    /**
     * Creates a new match with optional associated odds.
     *
     * @param req the match request containing match details and optional odds
     * @return the created match response with generated ID and odds if provided
     * @throws ConflictException if odds contain duplicate specifiers
     * @throws IllegalArgumentException if sport code is invalid
     */
    @Operation(
            summary = "Create match",
            description = "Creates a single Match. Optionally accepts an odds array that will be created together with the match.",
            operationId = "createMatch"
    )
    @ApiResponse(responseCode = "201", description = "Match created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse create(@Valid @RequestBody MatchRequest req) {
        return matchService.create(req);
    }

    /**
     * Creates multiple matches in a single transactional request.
     *
     * @param reqs list of match requests to create
     * @return list of created match responses with generated IDs
     * @throws ConflictException if any match odds contain duplicate specifiers
     */
    @Operation(
            summary = "Create matches in bulk",
            description = "Creates multiple Matches in a single request. Each match may optionally include odds. The operation is transactional.",
            operationId = "createMatchesBulk"
    )
    @ApiResponse(responseCode = "201", description = "Matches created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchResponse> createBulk(@RequestBody @Valid List<@Valid MatchRequest> reqs) {
        return matchService.createBulk(reqs);
    }

    /**
     * Retrieves a single match by ID with all associated odds.
     *
     * @param id the match ID to retrieve
     * @return the match response with odds included
     * @throws NotFoundException if match with given ID does not exist
     */
    @Operation(
            summary = "Get match by id",
            description = "Returns a single Match by id. This endpoint always includes odds (if any exist).",
            operationId = "getMatchById"
    )
    @ApiResponse(responseCode = "200", description = "Match found")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return matchService.get(id);
    }

    /**
     * Retrieves matches with pagination and optional odds inclusion.
     * <p>
     * This endpoint returns paginated results by default, following REST best practices.
     * All results are paginated for consistent behavior and better performance with large datasets.
     * </p>
     * Query parameters:
     * - page: 0-indexed page number (default: 0)
     * - size: page size (default: 20)
     * - sort: sort criteria in format property,asc|desc (e.g., id,desc, matchDate,desc)
     * - includeOdds: whether to include associated odds (default: false)
     * <p>
     * Examples:
     * <ul>
     *   <li>GET /api/matches - First page with 20 items</li>
     *   <li>GET /api/matches?page=1&size=50 - Page 2 with 50 items</li>
     *   <li>GET /api/matches?sort=matchDate,desc - Sorted by date, newest first</li>
     *   <li>GET /api/matches?includeOdds=true - Include match odds</li>
     *   <li>GET /api/matches?page=0&size=30&sort=matchDate,desc&includeOdds=true - All options combined</li>
     * </ul>
     *
     * @param includeOdds whether to include associated odds in the response (default: false)
     * @param pageable the pagination parameters (page, size, sort)
     * @return a page of match responses with pagination metadata
     */
    @Operation(
            summary = "List matches",
            description = "Returns paginated matches with optional odds. " +
                    "By default odds are not included. Use ?includeOdds=true to include odds. " +
                    "Supports sorting via sort parameter (e.g., sort=matchDate,desc).",
            operationId = "listMatches"
    )
    @ApiResponse(responseCode = "200", description = "Matches returned successfully with pagination metadata")
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @GetMapping
    public Page<MatchResponse> list(
            @Parameter(
                    name = "includeOdds",
                    description = "Whether to include associated odds in the response",
                    example = "false"
            )
            @RequestParam(defaultValue = "false") boolean includeOdds,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return matchService.listPage(includeOdds, pageable);
    }

    /**
     * Updates an existing match by ID with full replacement semantics.
     * If odds are provided, existing odds are replaced; if omitted, existing odds remain unchanged.
     *
     * @param id the match ID to update
     * @param req the match request with updated values
     * @return the updated match response
     * @throws NotFoundException if match with given ID does not exist
     * @throws ConflictException if odds contain duplicate specifiers
     */
    @Operation(
            summary = "Update match by id",
            description = "Replaces match fields with the provided payload. If odds are provided, they replace the existing odds collection. If odds are omitted, existing odds are left unchanged.",
            operationId = "updateMatchById"
    )
    @ApiResponse(responseCode = "200", description = "Match updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PutMapping("/{id}")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest req) {
        return matchService.update(id, req);
    }

    /**
     * Deletes a match by ID. Associated odds are automatically deleted due to cascade configuration.
     *
     * @param id the match ID to delete
     * @throws NotFoundException if match with given ID does not exist
     */
    @Operation(
            summary = "Delete match by id",
            description = "Deletes a match by id. Associated odds are deleted as well.",
            operationId = "deleteMatchById"
    )
    @ApiResponse(responseCode = "204", description = "Match deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        matchService.delete(id);
    }
}