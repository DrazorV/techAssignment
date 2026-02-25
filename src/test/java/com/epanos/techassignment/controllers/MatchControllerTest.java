package com.epanos.techassignment.controllers;

import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.enums.Sport;
import com.epanos.techassignment.services.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private MatchService matchService;

    private final MatchResponse sampleResponse = MatchResponse.builder()
            .id(1L)
            .description("OSFP-PAO")
            .matchDate(LocalDate.of(2024, 3, 31))
            .matchTime(LocalTime.of(18, 0))
            .teamA("OSFP")
            .teamB("PAO")
            .sport(Sport.FOOTBALL)
            .odds(List.of())
            .build();

    // ── POST /api/matches ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/matches → 201 on valid request")
    void create_success() throws Exception {
        when(matchService.create(any())).thenReturn(sampleResponse);

        String body = """
                {
                  "description": "OSFP-PAO",
                  "matchDate": "2024-03-31",
                  "matchTime": "18:00",
                  "teamA": "OSFP",
                  "teamB": "PAO",
                  "sport": 1
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("OSFP-PAO"));
    }

    @Test
    @DisplayName("POST /api/matches → 400 on missing required fields")
    void create_validation() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/matches → 400 on missing body")
    void create_missingBody() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ── GET /api/matches/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/matches/1 → 200 when found")
    void get_success() throws Exception {
        when(matchService.get(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/matches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.teamA").value("OSFP"));
    }

    @Test
    @DisplayName("GET /api/matches/99 → 404 when not found")
    void get_notFound() throws Exception {
        when(matchService.get(99L)).thenThrow(new NotFoundException("Match not found: 99"));

        mockMvc.perform(get("/api/matches/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ── GET /api/matches ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/matches → 200 with paginated results")
    void list_success() throws Exception {
        Page<MatchResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(matchService.listPage(eq(false), any())).thenReturn(page);

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/matches?includeOdds=true → passes includeOdds param")
    void list_withOdds() throws Exception {
        Page<MatchResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(matchService.listPage(eq(true), any())).thenReturn(page);

        mockMvc.perform(get("/api/matches").param("includeOdds", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(matchService).listPage(eq(true), any());
    }

    // ── PUT /api/matches/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/matches/1 → 200 on valid update")
    void update_success() throws Exception {
        when(matchService.update(eq(1L), any())).thenReturn(sampleResponse);

        String body = """
                {
                  "description": "OSFP-PAO",
                  "matchDate": "2024-03-31",
                  "matchTime": "18:00",
                  "teamA": "OSFP",
                  "teamB": "PAO",
                  "sport": 1
                }
                """;

        mockMvc.perform(put("/api/matches/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/matches/1 → 400 on missing body")
    void update_missingBody() throws Exception {
        mockMvc.perform(put("/api/matches/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("PUT /api/matches/99 → 404 when not found")
    void update_notFound() throws Exception {
        when(matchService.update(eq(99L), any())).thenThrow(new NotFoundException("Match not found: 99"));

        String body = """
                {
                  "description": "OSFP-PAO",
                  "matchDate": "2024-03-31",
                  "matchTime": "18:00",
                  "teamA": "OSFP",
                  "teamB": "PAO",
                  "sport": 1
                }
                """;

        mockMvc.perform(put("/api/matches/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/matches/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/matches/1 → 204 on success")
    void delete_success() throws Exception {
        doNothing().when(matchService).delete(1L);

        mockMvc.perform(delete("/api/matches/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/matches/99 → 404 when not found")
    void delete_notFound() throws Exception {
        doThrow(new NotFoundException("Match not found: 99")).when(matchService).delete(99L);

        mockMvc.perform(delete("/api/matches/99"))
                .andExpect(status().isNotFound());
    }
}

