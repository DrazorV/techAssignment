package com.epanos.techassignment.services;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.repositories.MatchOddsRepository;
import com.epanos.techassignment.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchOddsService {

    private final MatchRepository matchRepository;
    private final MatchOddsRepository matchOddsRepository;

    public MatchOddsResponse create(Long matchId, MatchOddsRequest req) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException("Match not found: " + matchId));

        // prevent duplicate specifier per match (also enforced by DB constraint)
        if (matchOddsRepository.existsByMatchIdAndSpecifier(matchId, req.getSpecifier())) {
            throw new ConflictException("Odds specifier already exists for match " + matchId + ": " + req.getSpecifier());
        }

        MatchOdds odds = MatchOdds.builder()
                .match(match)
                .specifier(req.getSpecifier())
                .odd(req.getOdd())
                .build();

        MatchOdds saved = matchOddsRepository.save(odds);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MatchOddsResponse> listByMatch(Long matchId) {
        // ensure match exists
        if (!matchRepository.existsById(matchId)) {
            throw new NotFoundException("Match not found: " + matchId);
        }
        return matchOddsRepository.findByMatchId(matchId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MatchOddsResponse get(Long matchId, Long oddId) {
        MatchOdds odds = matchOddsRepository.findByIdAndMatchId(oddId, matchId)
                .orElseThrow(() -> new NotFoundException("Odds not found: " + oddId + " for match " + matchId));
        return toResponse(odds);
    }

    public MatchOddsResponse update(Long matchId, Long oddId, MatchOddsRequest req) {
        MatchOdds odds = matchOddsRepository.findByIdAndMatchId(oddId, matchId)
                .orElseThrow(() -> new NotFoundException("Odds not found: " + oddId + " for match " + matchId));

        // if specifier changes, enforce uniqueness
        String newSpec = req.getSpecifier();
        if (!odds.getSpecifier().equals(newSpec) && matchOddsRepository.existsByMatchIdAndSpecifier(matchId, newSpec)) {
            throw new ConflictException("Odds specifier already exists for match " + matchId + ": " + newSpec);
        }

        odds.setSpecifier(req.getSpecifier());
        odds.setOdd(req.getOdd());

        return toResponse(odds);
    }

    public void delete(Long matchId, Long oddId) {
        MatchOdds odds = matchOddsRepository.findByIdAndMatchId(oddId, matchId)
                .orElseThrow(() -> new NotFoundException("Odds not found: " + oddId + " for match " + matchId));
        matchOddsRepository.delete(odds);
    }

    private MatchOddsResponse toResponse(MatchOdds odds) {
        return MatchOddsResponse.builder()
                .id(odds.getId())
                .matchId(odds.getMatch().getId())
                .specifier(odds.getSpecifier())
                .odd(odds.getOdd())
                .build();
    }
}