package com.epanos.techassignment.services;

import com.epanos.techassignment.exceptions.ConflictException;
import com.epanos.techassignment.exceptions.NotFoundException;
import com.epanos.techassignment.models.dto.MatchOddsRequest;
import com.epanos.techassignment.models.dto.MatchRequest;
import com.epanos.techassignment.models.dto.MatchResponse;
import com.epanos.techassignment.models.entities.Match;
import com.epanos.techassignment.models.entities.MatchOdds;
import com.epanos.techassignment.models.mappers.MatchMapper;
import com.epanos.techassignment.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper mapper;

    /**
     * Creates a new match with optional associated odds.
     *
     * @param req the match request containing match details and optional odds
     * @return the created match response with generated ID
     * @throws ConflictException if odds contain duplicate specifiers
     */
    public MatchResponse create(MatchRequest req) {
        validateOddsSpecifiers(req.getOdds());

        Match match = mapper.toEntity(req);
        addOddsToMatch(match, req.getOdds());

        Match saved = matchRepository.save(match);
        return mapper.toResponse(saved, true);
    }

    /**
     * Creates multiple matches in a single transactional operation.
     *
     * @param reqs list of match requests to create
     * @return list of created match responses with generated IDs
     * @throws ConflictException if any match odds contain duplicate specifiers
     */
    public List<MatchResponse> createBulk(List<MatchRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();

        List<Match> matches = reqs.stream().map(req -> {
            validateOddsSpecifiers(req.getOdds());
            Match match = mapper.toEntity(req);
            addOddsToMatch(match, req.getOdds());
            return match;
        }).toList();

        return matchRepository.saveAll(matches).stream()
                .map(m -> mapper.toResponse(m, true))
                .toList();
    }

    /**
     * Retrieves a match by ID with all associated odds.
     *
     * @param id the match ID
     * @return the match response with odds included
     * @throws NotFoundException if match not found
     */
    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        return mapper.toResponse(match, true);
    }

    /**
     * Retrieves all matches with optional odds inclusion (non-paginated).
     *
     * @param includeOdds whether to include odds in responses
     * @return list of match responses
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> list(boolean includeOdds) {

        if (includeOdds) {
            return matchRepository.findAllWithOdds().stream().map(m -> mapper.toResponse(m, true)).toList();
        }

        return matchRepository.findAll().stream().map(m -> mapper.toResponse(m, false)).toList();
    }

    /**
     * Retrieves a paginated list of matches with optional odds inclusion.
     *
     * @param includeOdds whether to include odds in responses
     * @param pageable the pagination parameters (page, size, sort)
     * @return a page of match responses
     */
    @Transactional(readOnly = true)
    public Page<MatchResponse> listPage(boolean includeOdds, Pageable pageable) {
        if (includeOdds) {
            return matchRepository.findAllWithOdds(pageable).map(m -> mapper.toResponse(m, true));
        }

        return matchRepository.findAll(pageable).map(m -> mapper.toResponse(m, false));
    }

    /**
     * Updates an existing match with full replacement semantics.
     * If odds are provided in the request, existing odds are replaced.
     * If odds are not provided (null), existing odds remain unchanged.
     *
     * @param id the match ID to update
     * @param req the match request with updated values
     * @return the updated match response
     * @throws NotFoundException if match not found
     * @throws ConflictException if odds contain duplicate specifiers
     */
    public MatchResponse update(Long id, MatchRequest req) {
        validateOddsSpecifiers(req.getOdds());

        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        mapper.updateEntity(match, req);

        if (req.getOdds() != null) {
            match.getOdds().clear();
            matchRepository.flush();
            addOddsToMatch(match, req.getOdds());
            matchRepository.flush();
        }

        return mapper.toResponse(match, true);
    }

    /**
     * Deletes a match by ID. Associated odds are automatically deleted due to cascade configuration.
     *
     * @param id the match ID to delete
     * @throws NotFoundException if match not found
     */
    public void delete(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        matchRepository.delete(match);
    }

    /**
     * Validates that odds within a request have unique specifiers.
     * Prevents duplicate specifiers in the same request payload.
     *
     * @param odds the list of odds to validate
     * @throws ConflictException if duplicate specifiers are found
     */
    private void validateOddsSpecifiers(List<MatchOddsRequest> odds) {
        if (odds == null) return;

        Set<String> seen = new HashSet<>();
        for (MatchOddsRequest o : odds) {
            String spec = (o.getSpecifier() == null) ? null : o.getSpecifier().trim();
            if (spec != null && !seen.add(spec)) {
                throw new ConflictException("Duplicate odds specifier in request payload: " + spec);
            }
        }
    }

    /**
     * Adds odds to a match entity. Creates the relationship between odds and match.
     *
     * @param match the match entity to add odds to
     * @param odds the list of odds to add (can be null)
     */
    private void addOddsToMatch(Match match, List<MatchOddsRequest> odds) {
        if (odds != null) {
            odds.forEach(o -> {
                MatchOdds odd = mapper.toEntity(o);
                odd.setMatch(match);
                match.getOdds().add(odd);
            });
        }
    }
}

