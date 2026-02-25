package com.epanos.techassignment.services;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.repositories.MatchOddsRepository;
import com.epanos.techassignment.repositories.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchOddsServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchOddsRepository matchOddsRepository;

    @InjectMocks
    private MatchOddsService matchOddsService;

    private Match match;
    private MatchOdds oddsEntity;
    private MatchOddsRequest oddsRequest;

    @BeforeEach
    void setUp() {
        match = Match.builder()
                .id(1L)
                .description("OSFP-PAO")
                .odds(new ArrayList<>())
                .build();

        oddsEntity = MatchOdds.builder()
                .id(10L)
                .match(match)
                .specifier("X")
                .odd(BigDecimal.valueOf(1.5))
                .build();

        oddsRequest = new MatchOddsRequest();
        oddsRequest.setSpecifier("X");
        oddsRequest.setOdd(BigDecimal.valueOf(1.5));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: should create odd successfully")
    void create_success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(1L, "X")).thenReturn(false);
        when(matchOddsRepository.save(any(MatchOdds.class))).thenReturn(oddsEntity);

        MatchOddsResponse result = matchOddsService.create(1L, oddsRequest);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getMatchId()).isEqualTo(1L);
        assertThat(result.getSpecifier()).isEqualTo("X");
        assertThat(result.getOdd()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("create: should throw NotFoundException when match not found")
    void create_matchNotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.create(99L, oddsRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Match not found: 99");
    }

    @Test
    @DisplayName("create: should throw ConflictException when specifier already exists")
    void create_duplicateSpecifier() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(1L, "X")).thenReturn(true);

        assertThatThrownBy(() -> matchOddsService.create(1L, oddsRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Odds specifier already exists");
    }

    // ── createBulk ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBulk: should return empty list for null input")
    void createBulk_nullInput() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        List<MatchOddsResponse> result = matchOddsService.createBulk(1L, null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createBulk: should return empty list for empty input")
    void createBulk_emptyInput() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        List<MatchOddsResponse> result = matchOddsService.createBulk(1L, List.of());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createBulk: should create multiple odds")
    void createBulk_success() {
        MatchOddsRequest req2 = new MatchOddsRequest();
        req2.setSpecifier("1");
        req2.setOdd(BigDecimal.valueOf(2.0));

        MatchOdds entity2 = MatchOdds.builder().id(11L).match(match).specifier("1").odd(BigDecimal.valueOf(2.0)).build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(eq(1L), anyString())).thenReturn(false);
        when(matchOddsRepository.saveAll(anyList())).thenReturn(List.of(oddsEntity, entity2));

        List<MatchOddsResponse> result = matchOddsService.createBulk(1L, List.of(oddsRequest, req2));

        assertThat(result).hasSize(2);
        verify(matchOddsRepository).flush();
    }

    @Test
    @DisplayName("createBulk: should throw ConflictException for duplicate specifiers in payload")
    void createBulk_duplicateInPayload() {
        MatchOddsRequest req2 = new MatchOddsRequest();
        req2.setSpecifier("X");
        req2.setOdd(BigDecimal.valueOf(3.0));

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchOddsService.createBulk(1L, List.of(oddsRequest, req2)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate odds specifier in request payload");
    }

    @Test
    @DisplayName("createBulk: should throw ConflictException when specifier already exists in DB")
    void createBulk_existsInDb() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(1L, "X")).thenReturn(true);

        assertThatThrownBy(() -> matchOddsService.createBulk(1L, List.of(oddsRequest)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Odds specifier already exists");
    }

    @Test
    @DisplayName("createBulk: should throw NotFoundException when match not found")
    void createBulk_matchNotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.createBulk(99L, List.of(oddsRequest)))
                .isInstanceOf(NotFoundException.class);
    }

    // ── get ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("get: should return odd when found")
    void get_found() {
        when(matchOddsRepository.findByIdAndMatchId(10L, 1L)).thenReturn(Optional.of(oddsEntity));

        MatchOddsResponse result = matchOddsService.get(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSpecifier()).isEqualTo("X");
    }

    @Test
    @DisplayName("get: should throw NotFoundException when not found")
    void get_notFound() {
        when(matchOddsRepository.findByIdAndMatchId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.get(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Odds not found");
    }

    // ── listByMatch ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByMatch: should return odds for existing match")
    void listByMatch_success() {
        when(matchRepository.existsById(1L)).thenReturn(true);
        when(matchOddsRepository.findByMatchId(1L)).thenReturn(List.of(oddsEntity));

        List<MatchOddsResponse> result = matchOddsService.listByMatch(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecifier()).isEqualTo("X");
    }

    @Test
    @DisplayName("listByMatch: should throw NotFoundException when match not found")
    void listByMatch_matchNotFound() {
        when(matchRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> matchOddsService.listByMatch(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── listByMatchPage ─────────────────────────────────────────────────────

    @Test
    @DisplayName("listByMatchPage: should return paginated odds")
    void listByMatchPage_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MatchOdds> page = new PageImpl<>(List.of(oddsEntity), pageable, 1);

        when(matchRepository.existsById(1L)).thenReturn(true);
        when(matchOddsRepository.findByMatchId(1L, pageable)).thenReturn(page);

        Page<MatchOddsResponse> result = matchOddsService.listByMatchPage(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getSpecifier()).isEqualTo("X");
    }

    @Test
    @DisplayName("listByMatchPage: should throw NotFoundException when match not found")
    void listByMatchPage_matchNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> matchOddsService.listByMatchPage(99L, pageable))
                .isInstanceOf(NotFoundException.class);
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: should update odd with same specifier")
    void update_sameSpecifier() {
        oddsRequest.setOdd(BigDecimal.valueOf(3.0));

        when(matchOddsRepository.findByIdAndMatchId(10L, 1L)).thenReturn(Optional.of(oddsEntity));

        MatchOddsResponse result = matchOddsService.update(1L, 10L, oddsRequest);

        assertThat(result.getOdd()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
        verify(matchOddsRepository, never()).existsByMatchIdAndSpecifier(anyLong(), anyString());
    }

    @Test
    @DisplayName("update: should update odd with new unique specifier")
    void update_newSpecifier() {
        MatchOddsRequest updateReq = new MatchOddsRequest();
        updateReq.setSpecifier("2");
        updateReq.setOdd(BigDecimal.valueOf(2.5));

        when(matchOddsRepository.findByIdAndMatchId(10L, 1L)).thenReturn(Optional.of(oddsEntity));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(1L, "2")).thenReturn(false);

        MatchOddsResponse result = matchOddsService.update(1L, 10L, updateReq);

        assertThat(result.getSpecifier()).isEqualTo("2");
    }

    @Test
    @DisplayName("update: should throw ConflictException when new specifier already exists")
    void update_duplicateSpecifier() {
        MatchOddsRequest updateReq = new MatchOddsRequest();
        updateReq.setSpecifier("1");
        updateReq.setOdd(BigDecimal.valueOf(2.0));

        when(matchOddsRepository.findByIdAndMatchId(10L, 1L)).thenReturn(Optional.of(oddsEntity));
        when(matchOddsRepository.existsByMatchIdAndSpecifier(1L, "1")).thenReturn(true);

        assertThatThrownBy(() -> matchOddsService.update(1L, 10L, updateReq))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Odds specifier already exists");
    }

    @Test
    @DisplayName("update: should throw NotFoundException when odd not found")
    void update_notFound() {
        when(matchOddsRepository.findByIdAndMatchId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.update(1L, 99L, oddsRequest))
                .isInstanceOf(NotFoundException.class);
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: should delete odd when found")
    void delete_success() {
        when(matchOddsRepository.findByIdAndMatchId(10L, 1L)).thenReturn(Optional.of(oddsEntity));

        matchOddsService.delete(1L, 10L);

        verify(matchOddsRepository).delete(oddsEntity);
    }

    @Test
    @DisplayName("delete: should throw NotFoundException when odd not found")
    void delete_notFound() {
        when(matchOddsRepository.findByIdAndMatchId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.delete(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── deleteAll ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteAll: should clear all odds from match")
    void deleteAll_success() {
        match.getOdds().add(oddsEntity);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        matchOddsService.deleteAll(1L);

        assertThat(match.getOdds()).isEmpty();
    }

    @Test
    @DisplayName("deleteAll: should throw NotFoundException when match not found")
    void deleteAll_notFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchOddsService.deleteAll(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
