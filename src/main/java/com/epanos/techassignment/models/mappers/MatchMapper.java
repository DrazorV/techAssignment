package com.epanos.techassignment.models.mappers;

import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MatchMapper {

    public Match toEntity(MatchRequest req) {
        return Match.builder()
                .description(req.getDescription())
                .matchDate(req.getMatchDate())
                .matchTime(req.getMatchTime())
                .teamA(req.getTeamA())
                .teamB(req.getTeamB())
                .sport(req.getSport())
                .build();
    }

    public void updateEntity(Match match, MatchRequest req) {
        match.setDescription(req.getDescription());
        match.setMatchDate(req.getMatchDate());
        match.setMatchTime(req.getMatchTime());
        match.setTeamA(req.getTeamA());
        match.setTeamB(req.getTeamB());
        match.setSport(req.getSport());
    }

    public MatchOdds toEntity(MatchOddsRequest req) {
        return MatchOdds.builder()
                .specifier(req.getSpecifier().trim())
                .odd(req.getOdd())
                .build();
    }

    public MatchOddsResponse toOddsResponse(MatchOdds odds) {
        return MatchOddsResponse.builder()
                .id(odds.getId())
                .matchId(odds.getMatch().getId())
                .specifier(odds.getSpecifier())
                .odd(odds.getOdd())
                .build();
    }

    public MatchResponse toResponse(Match match, boolean includeOdds) {
        List<MatchOddsResponse> odds = null;

        if (includeOdds) {
            odds = match.getOdds().stream()
                    .sorted(Comparator.comparing(MatchOdds::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(this::toOddsResponse)
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