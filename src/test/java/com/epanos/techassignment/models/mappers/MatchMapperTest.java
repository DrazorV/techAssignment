package com.epanos.techassignment.models.mappers;

import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.models.enums.Sport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MatchMapperTest {

    private final MatchMapper mapper = new MatchMapper();

    @Test
    @DisplayName("toEntity(MatchRequest): should map all fields from request to entity")
    void toMatchEntity_mapsAllFields() {
        MatchRequest req = new MatchRequest();
        req.setDescription("OSFP-PAO");
        req.setMatchDate(LocalDate.of(2024, 3, 31));
        req.setMatchTime(LocalTime.of(18, 0));
        req.setTeamA("OSFP");
        req.setTeamB("PAO");
        req.setSport(Sport.FOOTBALL);

        Match entity = mapper.toEntity(req);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDescription()).isEqualTo("OSFP-PAO");
        assertThat(entity.getMatchDate()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(entity.getMatchTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(entity.getTeamA()).isEqualTo("OSFP");
        assertThat(entity.getTeamB()).isEqualTo("PAO");
        assertThat(entity.getSport()).isEqualTo(Sport.FOOTBALL);
    }

    @Test
    @DisplayName("updateEntity: should update all fields on existing entity")
    void updateEntity_updatesAllFields() {
        Match match = Match.builder()
                .id(1L)
                .description("OLD")
                .matchDate(LocalDate.of(2020, 1, 1))
                .matchTime(LocalTime.of(10, 0))
                .teamA("A")
                .teamB("B")
                .sport(Sport.FOOTBALL)
                .odds(new ArrayList<>())
                .build();

        MatchRequest req = new MatchRequest();
        req.setDescription("NEW");
        req.setMatchDate(LocalDate.of(2025, 6, 15));
        req.setMatchTime(LocalTime.of(20, 30));
        req.setTeamA("C");
        req.setTeamB("D");
        req.setSport(Sport.BASKETBALL);

        mapper.updateEntity(match, req);

        assertThat(match.getId()).isEqualTo(1L);
        assertThat(match.getDescription()).isEqualTo("NEW");
        assertThat(match.getMatchDate()).isEqualTo(LocalDate.of(2025, 6, 15));
        assertThat(match.getMatchTime()).isEqualTo(LocalTime.of(20, 30));
        assertThat(match.getTeamA()).isEqualTo("C");
        assertThat(match.getTeamB()).isEqualTo("D");
        assertThat(match.getSport()).isEqualTo(Sport.BASKETBALL);
    }

    @Test
    @DisplayName("toEntity(MatchOddsRequest): should map and trim specifier")
    void toOddsEntity_mapsAndTrims() {
        MatchOddsRequest req = new MatchOddsRequest();
        req.setSpecifier("  X  ");
        req.setOdd(BigDecimal.valueOf(1.5));

        MatchOdds entity = mapper.toEntity(req);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getSpecifier()).isEqualTo("X");
        assertThat(entity.getOdd()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(entity.getMatch()).isNull();
    }

    @Test
    @DisplayName("toOddsResponse: should map all fields from entity to response")
    void toOddsResponse_mapsAllFields() {
        Match match = Match.builder().id(1L).build();
        MatchOdds entity = MatchOdds.builder()
                .id(10L)
                .match(match)
                .specifier("X")
                .odd(BigDecimal.valueOf(1.5))
                .build();

        MatchOddsResponse response = mapper.toOddsResponse(entity);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getMatchId()).isEqualTo(1L);
        assertThat(response.getSpecifier()).isEqualTo("X");
        assertThat(response.getOdd()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("toResponse: should map match with odds when includeOdds=true")
    void toResponse_withOdds() {
        Match match = Match.builder()
                .id(1L).description("OSFP-PAO")
                .matchDate(LocalDate.of(2024, 3, 31))
                .matchTime(LocalTime.of(18, 0))
                .teamA("OSFP").teamB("PAO")
                .sport(Sport.FOOTBALL)
                .odds(new ArrayList<>())
                .build();

        MatchOdds odds1 = MatchOdds.builder().id(2L).match(match).specifier("1").odd(BigDecimal.valueOf(1.2)).build();
        MatchOdds odds2 = MatchOdds.builder().id(1L).match(match).specifier("X").odd(BigDecimal.valueOf(3.0)).build();
        match.getOdds().addAll(List.of(odds1, odds2));

        MatchResponse response = mapper.toResponse(match, true);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOdds()).hasSize(2);
        assertThat(response.getOdds().get(0).getId()).isEqualTo(1L);
        assertThat(response.getOdds().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toResponse: should return null odds when includeOdds=false")
    void toResponse_withoutOdds() {
        Match match = Match.builder()
                .id(1L).description("OSFP-PAO")
                .matchDate(LocalDate.of(2024, 3, 31))
                .matchTime(LocalTime.of(18, 0))
                .teamA("OSFP").teamB("PAO")
                .sport(Sport.FOOTBALL)
                .odds(new ArrayList<>())
                .build();

        MatchResponse response = mapper.toResponse(match, false);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOdds()).isNull();
    }

    @Test
    @DisplayName("toResponse: should return empty list when match has no odds and includeOdds=true")
    void toResponse_emptyOdds() {
        Match match = Match.builder()
                .id(1L).description("Test")
                .matchDate(LocalDate.of(2024, 1, 1))
                .matchTime(LocalTime.of(12, 0))
                .teamA("A").teamB("B")
                .sport(Sport.BASKETBALL)
                .odds(new ArrayList<>())
                .build();

        MatchResponse response = mapper.toResponse(match, true);

        assertThat(response.getOdds()).isEmpty();
    }
}
