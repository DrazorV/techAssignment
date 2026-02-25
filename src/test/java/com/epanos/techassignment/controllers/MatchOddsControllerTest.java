package com.epanos.techassignment.controllers;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsResponse;
import com.epanos.techassignment.services.MatchOddsService;
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

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchOddsController.class)
@WithMockUser
class MatchOddsControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private MatchOddsService matchOddsService;

    private final MatchOddsResponse sampleResponse = MatchOddsResponse.builder()
            .id(10L)
            .matchId(1L)
            .specifier("X")
            .odd(BigDecimal.valueOf(1.5))
            .build();

    // ── POST /api/matches/{matchId}/odds ────────────────────────────────────

    @Test
    @DisplayName("POST /api/matches/1/odds → 201 on valid request")
    void create_success() throws Exception {
        when(matchOddsService.create(eq(1L), any())).thenReturn(sampleResponse);

        String body = """
                {
                  "specifier": "X",
                  "odd": 1.5
                }
                """;

        mockMvc.perform(post("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.specifier").value("X"));
    }

    @Test
    @DisplayName("POST /api/matches/1/odds → 400 on missing fields")
    void create_validation() throws Exception {
        mockMvc.perform(post("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/matches/99/odds → 404 when match not found")
    void create_matchNotFound() throws Exception {
        when(matchOddsService.create(eq(99L), any()))
                .thenThrow(new NotFoundException("Match not found: 99"));

        String body = """
                {
                  "specifier": "X",
                  "odd": 1.5
                }
                """;

        mockMvc.perform(post("/api/matches/99/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/matches/1/odds → 409 on duplicate specifier")
    void create_conflict() throws Exception {
        when(matchOddsService.create(eq(1L), any()))
                .thenThrow(new ConflictException("Odds specifier already exists for match 1: X"));

        String body = """
                {
                  "specifier": "X",
                  "odd": 1.5
                }
                """;

        mockMvc.perform(post("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("POST /api/matches/1/odds → 400 on missing body")
    void create_missingBody() throws Exception {
        mockMvc.perform(post("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ── GET /api/matches/{matchId}/odds/{oddId} ─────────────────────────────

    @Test
    @DisplayName("GET /api/matches/1/odds/10 → 200 when found")
    void get_success() throws Exception {
        when(matchOddsService.get(1L, 10L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/matches/1/odds/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.matchId").value(1));
    }

    @Test
    @DisplayName("GET /api/matches/1/odds/99 → 404 when not found")
    void get_notFound() throws Exception {
        when(matchOddsService.get(1L, 99L))
                .thenThrow(new NotFoundException("Odds not found: 99 for match 1"));

        mockMvc.perform(get("/api/matches/1/odds/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/matches/{matchId}/odds ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/matches/1/odds → 200 with paginated results")
    void list_success() throws Exception {
        Page<MatchOddsResponse> page = new PageImpl<>(
                List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(matchOddsService.listByMatchPage(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/matches/1/odds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].specifier").value("X"));
    }

    @Test
    @DisplayName("GET /api/matches/99/odds → 404 when match not found")
    void list_matchNotFound() throws Exception {
        when(matchOddsService.listByMatchPage(eq(99L), any()))
                .thenThrow(new NotFoundException("Match not found: 99"));

        mockMvc.perform(get("/api/matches/99/odds"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/matches/{matchId}/odds/{oddId} ─────────────────────────────

    @Test
    @DisplayName("PUT /api/matches/1/odds/10 → 200 on valid update")
    void update_success() throws Exception {
        MatchOddsResponse updated = MatchOddsResponse.builder()
                .id(10L).matchId(1L).specifier("2").odd(BigDecimal.valueOf(3.0)).build();
        when(matchOddsService.update(eq(1L), eq(10L), any())).thenReturn(updated);

        String body = """
                {
                  "specifier": "2",
                  "odd": 3.0
                }
                """;

        mockMvc.perform(put("/api/matches/1/odds/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specifier").value("2"));
    }

    @Test
    @DisplayName("PUT /api/matches/1/odds/10 → 400 on missing body")
    void update_missingBody() throws Exception {
        mockMvc.perform(put("/api/matches/1/odds/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("PUT /api/matches/1/odds/99 → 404 when not found")
    void update_notFound() throws Exception {
        when(matchOddsService.update(eq(1L), eq(99L), any()))
                .thenThrow(new NotFoundException("Odds not found: 99 for match 1"));

        String body = """
                {
                  "specifier": "X",
                  "odd": 1.5
                }
                """;

        mockMvc.perform(put("/api/matches/1/odds/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/matches/{matchId}/odds (by specifier in body) ─────────────

    @Test
    @DisplayName("PUT /api/matches/1/odds → 200 on valid update by specifier")
    void updateBySpecifier_success() throws Exception {
        MatchOddsResponse updated = MatchOddsResponse.builder()
                .id(10L).matchId(1L).specifier("X").odd(BigDecimal.valueOf(5.0)).build();
        when(matchOddsService.updateBySpecifier(eq(1L), any())).thenReturn(updated);

        String body = """
                {
                  "specifier": "X",
                  "odd": 5.0
                }
                """;

        mockMvc.perform(put("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specifier").value("X"))
                .andExpect(jsonPath("$.odd").value(5.0));
    }

    @Test
    @DisplayName("PUT /api/matches/1/odds → 404 when specifier not found")
    void updateBySpecifier_notFound() throws Exception {
        when(matchOddsService.updateBySpecifier(eq(1L), any()))
                .thenThrow(new NotFoundException("Odds with specifier 'Z' not found for match 1"));

        String body = """
                {
                  "specifier": "Z",
                  "odd": 1.5
                }
                """;

        mockMvc.perform(put("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/matches/1/odds → 400 on missing body")
    void updateBySpecifier_missingBody() throws Exception {
        mockMvc.perform(put("/api/matches/1/odds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ── DELETE /api/matches/{matchId}/odds/{oddId} ──────────────────────────

    @Test
    @DisplayName("DELETE /api/matches/1/odds/10 → 204 on success")
    void delete_success() throws Exception {
        doNothing().when(matchOddsService).delete(1L, 10L);

        mockMvc.perform(delete("/api/matches/1/odds/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/matches/1/odds/99 → 404 when not found")
    void delete_notFound() throws Exception {
        doThrow(new NotFoundException("Odds not found: 99 for match 1"))
                .when(matchOddsService).delete(1L, 99L);

        mockMvc.perform(delete("/api/matches/1/odds/99"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/matches/{matchId}/odds ───────────────────────────────────

    @Test
    @DisplayName("DELETE /api/matches/1/odds → 204 on success")
    void deleteAll_success() throws Exception {
        doNothing().when(matchOddsService).deleteAll(1L);

        mockMvc.perform(delete("/api/matches/1/odds"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/matches/99/odds → 404 when match not found")
    void deleteAll_notFound() throws Exception {
        doThrow(new NotFoundException("Match not found: 99"))
                .when(matchOddsService).deleteAll(99L);

        mockMvc.perform(delete("/api/matches/99/odds"))
                .andExpect(status().isNotFound());
    }
}

