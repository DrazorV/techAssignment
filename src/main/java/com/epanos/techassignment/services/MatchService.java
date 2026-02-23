package com.epanos.techassignment.services;

import com.epanos.techassignment.configs.NotFoundException;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchResponse create(MatchRequest req) {
        Match match = Match.builder()
                .description(req.getDescription())
                .matchDate(req.getMatchDate())
                .matchTime(req.getMatchTime())
                .teamA(req.getTeamA())
                .teamB(req.getTeamB())
                .sport(req.getSport())
                .build();

        Match saved = matchRepository.save(match);
        return toResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        return toResponse(match, true);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> list() {
        return matchRepository.findAll().stream().map(m -> toResponse(m, false)).toList();
    }

    public MatchResponse update(Long id, MatchRequest req) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));

        match.setDescription(req.getDescription());
        match.setMatchDate(req.getMatchDate());
        match.setMatchTime(req.getMatchTime());
        match.setTeamA(req.getTeamA());
        match.setTeamB(req.getTeamB());
        match.setSport(req.getSport());

        return toResponse(match, true);
    }

    public void delete(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        matchRepository.delete(match); // cascades odds delete
    }

    private MatchResponse toResponse(Match match, boolean includeOdds) {
        List<MatchOddsResponse> odds = null;

        if (includeOdds) {
            odds = match.getOdds().stream()
                    .sorted(Comparator.comparing(MatchOdds::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(o -> MatchOddsResponse.builder()
                            .id(o.getId())
                            .matchId(match.getId())
                            .specifier(o.getSpecifier())
                            .odd(o.getOdd())
                            .build())
                    .toList();
        }

        return MatchResponse.builder()
                .id(match.getId())
                .description(match.getDescription())
                .matchDate(match.getMatchDate())
                .matchTime(match.getMatchTime())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .sport(match.getSport())
                .odds(odds)
                .build();
    }
}