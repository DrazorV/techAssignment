package com.epanos.techassignment.controllers;

import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.services.MatchOddsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches/{matchId}/odds")
@RequiredArgsConstructor
@Tag(name = "Match Odds", description = "Operations related to match odds")
public class MatchOddsController {

    private final MatchOddsService matchOddsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchOddsResponse create(@PathVariable Long matchId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.create(matchId, request);
    }

    @PutMapping("/{oddId}")
    public MatchOddsResponse update(@PathVariable Long matchId, @PathVariable Long oddId, @Valid @RequestBody MatchOddsRequest request) {
        return matchOddsService.update(matchId, oddId, request);
    }

    @DeleteMapping("/{oddId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long matchId, @PathVariable Long oddId) {
        matchOddsService.delete(matchId, oddId);
    }
}
