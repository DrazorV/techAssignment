package com.epanos.techassignment.controllers;

import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.services.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "CRUD operations for Matches")
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create match")
    public MatchResponse create(@Valid @RequestBody MatchRequest req) {
        return matchService.create(req);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by id (includes odds)")
    public MatchResponse get(@PathVariable Long id) {
        return matchService.get(id);
    }

    @GetMapping
    @Operation(summary = "List matches")
    public List<MatchResponse> list() {
        return matchService.list();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update match by id (full update)")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest req) {
        return matchService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete match by id (also deletes odds)")
    public void delete(@PathVariable Long id) {
        matchService.delete(id);
    }
}