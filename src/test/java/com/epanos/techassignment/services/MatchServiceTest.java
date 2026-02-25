package com.epanos.techassignment.services;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.models.enums.Sport;
import com.epanos.techassignment.models.mappers.MatchMapper;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchMapper mapper;

    @InjectMocks
    private MatchService matchService;

    private MatchRequest matchRequest;
    private Match matchEntity;
    private MatchResponse matchResponse;

    @BeforeEach
    void setUp() {
        matchRequest = new MatchRequest();
        matchRequest.setDescription("OSFP-PAO");
        matchRequest.setMatchDate(LocalDate.of(2024, 3, 31));
        matchRequest.setMatchTime(LocalTime.of(18, 0));
        matchRequest.setTeamA("OSFP");
        matchRequest.setTeamB("PAO");
        matchRequest.setSport(Sport.FOOTBALL);

        matchEntity = Match.builder()
                .id(1L)
                .description("OSFP-PAO")
                .matchDate(LocalDate.of(2024, 3, 31))
                .matchTime(LocalTime.of(18, 0))
                .teamA("OSFP")
                .teamB("PAO")
                .sport(Sport.FOOTBALL)
                .odds(new ArrayList<>())
                .build();

        matchResponse = MatchResponse.builder()
                .id(1L)
                .description("OSFP-PAO")
                .matchDate(LocalDate.of(2024, 3, 31))
                .matchTime(LocalTime.of(18, 0))
                .teamA("OSFP")
                .teamB("PAO")
                .sport(Sport.FOOTBALL)
                .odds(List.of())
                .build();
    }

    @Test
    @DisplayName("create: should create match without odds")
    void create_withoutOdds() {
        when(mapper.toEntity(matchRequest)).thenReturn(matchEntity);
        when(matchRepository.save(matchEntity)).thenReturn(matchEntity);
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        MatchResponse result = matchService.create(matchRequest);

        assertThat(result).isEqualTo(matchResponse);
        verify(matchRepository).save(matchEntity);
    }

    @Test
    @DisplayName("create: should create match with valid odds")
    void create_withOdds() {
        MatchOddsRequest oddsReq = new MatchOddsRequest();
        oddsReq.setSpecifier("X");
        oddsReq.setOdd(BigDecimal.valueOf(1.5));
        matchRequest.setOdds(List.of(oddsReq));

        MatchOdds oddsEntity = MatchOdds.builder().specifier("X").odd(BigDecimal.valueOf(1.5)).build();

        when(mapper.toEntity(matchRequest)).thenReturn(matchEntity);
        when(mapper.toEntity(oddsReq)).thenReturn(oddsEntity);
        when(matchRepository.save(matchEntity)).thenReturn(matchEntity);
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        MatchResponse result = matchService.create(matchRequest);

        assertThat(result).isEqualTo(matchResponse);
        assertThat(matchEntity.getOdds()).hasSize(1);
        assertThat(matchEntity.getOdds().get(0).getMatch()).isEqualTo(matchEntity);
    }

    @Test
    @DisplayName("create: should throw ConflictException for duplicate specifiers")
    void create_duplicateSpecifiers() {
        MatchOddsRequest odds1 = new MatchOddsRequest();
        odds1.setSpecifier("X");
        odds1.setOdd(BigDecimal.valueOf(1.5));
        MatchOddsRequest odds2 = new MatchOddsRequest();
        odds2.setSpecifier("X");
        odds2.setOdd(BigDecimal.valueOf(2.0));
        matchRequest.setOdds(List.of(odds1, odds2));

        assertThatThrownBy(() -> matchService.create(matchRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate odds specifier");
    }

    @Test
    @DisplayName("createBulk: should return empty list for null input")
    void createBulk_nullInput() {
        List<MatchResponse> result = matchService.createBulk(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(matchRepository);
    }

    @Test
    @DisplayName("createBulk: should return empty list for empty input")
    void createBulk_emptyInput() {
        List<MatchResponse> result = matchService.createBulk(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(matchRepository);
    }

    @Test
    @DisplayName("createBulk: should create multiple matches")
    void createBulk_success() {
        MatchRequest req2 = new MatchRequest();
        req2.setDescription("AEK-OLY");
        req2.setMatchDate(LocalDate.of(2024, 4, 1));
        req2.setMatchTime(LocalTime.of(20, 0));
        req2.setTeamA("AEK");
        req2.setTeamB("OLY");
        req2.setSport(Sport.BASKETBALL);

        Match entity2 = Match.builder().id(2L).description("AEK-OLY").odds(new ArrayList<>()).build();
        MatchResponse resp2 = MatchResponse.builder().id(2L).description("AEK-OLY").build();

        when(mapper.toEntity(matchRequest)).thenReturn(matchEntity);
        when(mapper.toEntity(req2)).thenReturn(entity2);
        when(matchRepository.saveAll(anyList())).thenReturn(List.of(matchEntity, entity2));
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);
        when(mapper.toResponse(entity2, true)).thenReturn(resp2);

        List<MatchResponse> result = matchService.createBulk(List.of(matchRequest, req2));

        assertThat(result).hasSize(2);
        verify(matchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("get: should return match when found")
    void get_found() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        MatchResponse result = matchService.get(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("get: should throw NotFoundException when not found")
    void get_notFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Match not found: 99");
    }

    @Test
    @DisplayName("list: should list without odds")
    void list_withoutOdds() {
        when(matchRepository.findAll()).thenReturn(List.of(matchEntity));
        when(mapper.toResponse(matchEntity, false)).thenReturn(matchResponse);

        List<MatchResponse> result = matchService.list(false);

        assertThat(result).hasSize(1);
        verify(matchRepository).findAll();
        verify(matchRepository, never()).findAllWithOdds();
    }

    @Test
    @DisplayName("list: should list with odds")
    void list_withOdds() {
        when(matchRepository.findAllWithOdds()).thenReturn(List.of(matchEntity));
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        List<MatchResponse> result = matchService.list(true);

        assertThat(result).hasSize(1);
        verify(matchRepository).findAllWithOdds();
    }

    @Test
    @DisplayName("listPage: should return paginated results without odds")
    void listPage_withoutOdds() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> page = new PageImpl<>(List.of(matchEntity), pageable, 1);

        when(matchRepository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(matchEntity, false)).thenReturn(matchResponse);

        Page<MatchResponse> result = matchService.listPage(false, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listPage: should return paginated results with odds")
    void listPage_withOdds() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idsPage = new PageImpl<>(List.of(1L), pageable, 1);

        when(matchRepository.findAllIds(pageable)).thenReturn(idsPage);
        when(matchRepository.findAllWithOddsByIds(List.of(1L))).thenReturn(List.of(matchEntity));
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        Page<MatchResponse> result = matchService.listPage(true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        verify(matchRepository).findAllIds(pageable);
        verify(matchRepository).findAllWithOddsByIds(List.of(1L));
    }

    @Test
    @DisplayName("listPage: should return empty page with odds when no matches exist")
    void listPage_withOdds_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(matchRepository.findAllIds(pageable)).thenReturn(emptyPage);

        Page<MatchResponse> result = matchService.listPage(true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
        verify(matchRepository, never()).findAllWithOddsByIds(anyList());
    }

    @Test
    @DisplayName("update: should update match fields without replacing odds")
    void update_withoutOdds() {
        matchRequest.setOdds(null);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        MatchResponse result = matchService.update(1L, matchRequest);

        assertThat(result).isEqualTo(matchResponse);
        verify(mapper).updateEntity(matchEntity, matchRequest);
        verify(matchRepository, never()).flush();
    }

    @Test
    @DisplayName("update: should update match and replace odds when provided")
    void update_withOdds() {
        MatchOddsRequest oddsReq = new MatchOddsRequest();
        oddsReq.setSpecifier("1");
        oddsReq.setOdd(BigDecimal.valueOf(2.0));
        matchRequest.setOdds(List.of(oddsReq));

        MatchOdds oddsEntity = MatchOdds.builder().specifier("1").odd(BigDecimal.valueOf(2.0)).build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));
        when(mapper.toEntity(oddsReq)).thenReturn(oddsEntity);
        when(mapper.toResponse(matchEntity, true)).thenReturn(matchResponse);

        matchService.update(1L, matchRequest);

        verify(mapper).updateEntity(matchEntity, matchRequest);
        verify(matchRepository, times(2)).flush();
    }

    @Test
    @DisplayName("update: should throw NotFoundException when match not found")
    void update_notFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.update(99L, matchRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("update: should throw ConflictException for duplicate specifiers")
    void update_duplicateSpecifiers() {
        MatchOddsRequest o1 = new MatchOddsRequest();
        o1.setSpecifier("1");
        o1.setOdd(BigDecimal.ONE);
        MatchOddsRequest o2 = new MatchOddsRequest();
        o2.setSpecifier("1");
        o2.setOdd(BigDecimal.TEN);
        matchRequest.setOdds(List.of(o1, o2));

        assertThatThrownBy(() -> matchService.update(1L, matchRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("delete: should delete match when found")
    void delete_found() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEntity));
        matchService.delete(1L);
        verify(matchRepository).delete(matchEntity);
    }

    @Test
    @DisplayName("delete: should throw NotFoundException when match not found")
    void delete_notFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
