package com.epanos.techassignment.controllers;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.services.MatchOddsService;
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
@RequestMapping("/api/matches/{matchId}/odds")
@RequiredArgsConstructor
@Tag(name = "Match Odds",
        description = "Endpoints for managing odds associated with a specific match. Each odd is uniquely identified per match by its specifier."
)
public class MatchOddsController {

    private final MatchOddsService matchOddsService;

    /**
     * Creates a new odd for the specified match.
     * Each match cannot have duplicate specifiers.
     *
     * @param matchId the ID of the match to add odds to
     * @param request the match odds request containing specifier and odd value
     * @return the created match odds response with generated ID
     * @throws NotFoundException if match with given ID does not exist
     * @throws ConflictException if a specifier already exists for this match
     */
    @Operation(
            summary = "Create match odd",
            description = "Creates a new odd for the specified match. Each match cannot have duplicate specifiers.",
            operationId = "createMatchOdd"
    )
    @ApiResponse(responseCode = "201", description = "Match odd created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchOddsResponse create(@PathVariable Long matchId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.create(matchId, request);
    }

    /**
     * Creates multiple odds for the specified match in a single transactional request.
     * The operation is all-or-nothing: if any odd fails validation, all are rejected.
     * Specifiers must be unique within the same match.
     *
     * @param matchId the ID of the match to add odds to
     * @param requests list of match odds requests
     * @return list of created match odds responses with generated IDs
     * @throws NotFoundException if match with given ID does not exist
     * @throws ConflictException if duplicate specifiers found or specifier already exists for this match
     */
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
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchOddsResponse> createBulk(
            @Parameter(description = "Match id", example = "1", required = true)
            @PathVariable Long matchId,
            @Valid @RequestBody List<MatchOddsRequest> requests
    ) {
        return matchOddsService.createBulk(matchId, requests);
    }

    /**
     * Updates an existing odd for the specified match.
     * Performs full replacement of the odd fields (both specifier and value).
     *
     * @param matchId the ID of the match containing the odd
     * @param oddId the ID of the odd to update
     * @param request the updated match odds request
     * @return the updated match odds response
     * @throws NotFoundException if match or odd with given IDs do not exist
     * @throws ConflictException if a new specifier already exists for this match
     */
    @Operation(
            summary = "Update match odd",
            description = "Updates an existing odd for the specified match. Performs a full replacement of the odd fields.",
            operationId = "updateMatchOdd"
    )
    @ApiResponse(responseCode = "200", description = "Match odd updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PutMapping("/{oddId}")
    public MatchOddsResponse update(@PathVariable Long matchId, @PathVariable Long oddId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.update(matchId, oddId, request);
    }

    /**
     * Retrieves a specific odd belonging to the specified match.
     *
     * @param matchId the ID of the match containing the odd
     * @param oddId the ID of the odd to retrieve
     * @return the match odds response
     * @throws NotFoundException if match or odd with given IDs do not exist
     */
    @Operation(
            summary = "Get match odd by id",
            description = "Returns a specific odd belonging to the specified match.",
            operationId = "getMatchOddById"
    )
    @ApiResponse(responseCode = "200", description = "Match odd returned successfully")
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @GetMapping("/{oddId}")
    public MatchOddsResponse get(
            @Parameter(description = "Match id", example = "1", required = true)
            @PathVariable Long matchId,
            @Parameter(description = "Odd id", example = "10", required = true)
            @PathVariable Long oddId) {
        return matchOddsService.get(matchId, oddId);
    }

    /**
     * Retrieves paginated odds for the specified match.
     * <p>
     * This endpoint returns paginated results by default, following REST best practices.
     * All results are paginated for consistent behavior and better performance with large datasets.
     * </p>
     * Query parameters:
     * - page: 0-indexed page number (default: 0)
     * - size: page size (default: 20)
     * - sort: sort criteria in format property,asc|desc (e.g., id,asc, odd,desc)
     * <p>
     * Examples:
     * <ul>
     *   <li>GET /api/matches/1/odds - First page with 20 odds for match 1</li>
     *   <li>GET /api/matches/1/odds?page=1&size=10 - Page 2 with 10 odds</li>
     *   <li>GET /api/matches/1/odds?sort=odd,desc - Odds sorted by value, highest first</li>
     *   <li>GET /api/matches/1/odds?sort=specifier,asc - Odds sorted by specifier alphabetically</li>
     * </ul>
     *
     * @param matchId the ID of the match whose odds to retrieve
     * @param pageable the pagination parameters (page, size, sort)
     * @return a page of match odds responses with pagination metadata
     * @throws NotFoundException if match with given ID does not exist
     */
    @Operation(
            summary = "List match odds",
            description = "Returns paginated odds for the specified match. " +
                    "Supports pagination via page, size, and sort query parameters.",
            operationId = "listMatchOdds"
    )
    @ApiResponse(responseCode = "200", description = "Odds returned successfully with pagination metadata")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @GetMapping
    public Page<MatchOddsResponse> listByMatch(
            @Parameter(description = "Match id", example = "1", required = true) @PathVariable Long matchId,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return matchOddsService.listByMatchPage(matchId, pageable);
    }

    /**
     * Deletes an existing odd from the specified match.
     * The odd is permanently removed and cannot be recovered.
     *
     * @param matchId the ID of the match containing the odd
     * @param oddId the ID of the odd to delete
     * @throws NotFoundException if match or odd with given IDs do not exist
     */
    @Operation(
            summary = "Delete match odd",
            description = "Deletes an existing odd from the specified match.",
            operationId = "deleteMatchOdd"
    )
    @ApiResponse(responseCode = "204", description = "Match odd deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match or odd not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @DeleteMapping("/{oddId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long matchId, @PathVariable Long oddId) {
        matchOddsService.delete(matchId, oddId);
    }

    /**
     * Deletes all odds associated with the specified match.
     * All odds for the match are permanently removed in a single operation.
     *
     * @param matchId the ID of the match whose odds should be deleted
     * @throws NotFoundException if match with given ID does not exist
     */
    @Operation(
            summary = "Delete all match odds",
            description = "Deletes all odds associated with the specified match.",
            operationId = "deleteAllMatchOdds"
    )
    @ApiResponse(responseCode = "204", description = "All match odds deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@Parameter(description = "Match id", example = "1", required = true) @PathVariable Long matchId) {
        matchOddsService.deleteAll(matchId);
    }
}
