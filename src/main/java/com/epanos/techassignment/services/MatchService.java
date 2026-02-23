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

    public MatchResponse create(MatchRequest req) {
        validateOddsSpecifiers(req.getOdds());

        Match match = mapper.toEntity(req);

        if (req.getOdds() != null) {
            for (MatchOddsRequest o : req.getOdds()) {
                MatchOdds odd = mapper.toEntity(o);
                odd.setMatch(match);
                match.getOdds().add(odd);
            }
        }

        Match saved = matchRepository.save(match);
        return mapper.toResponse(saved, true);
    }

    public List<MatchResponse> createBulk(List<MatchRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();

        List<Match> matches = reqs.stream().map(req -> {
            validateOddsSpecifiers(req.getOdds());
            Match match = mapper.toEntity(req);
            if (req.getOdds() != null) {
                req.getOdds().forEach(o -> {
                    MatchOdds odd = mapper.toEntity(o);
                    odd.setMatch(match);
                    match.getOdds().add(odd);
                });
            }
            return match;
        }).toList();

        return matchRepository.saveAll(matches).stream()
                .map(m -> mapper.toResponse(m, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        return mapper.toResponse(match, true);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> list(boolean includeOdds) {

        if (includeOdds) {
            return matchRepository.findAllWithOdds().stream().map(m -> mapper.toResponse(m, true)).toList();
        }

        return matchRepository.findAll().stream().map(m -> mapper.toResponse(m, false)).toList();
    }

    public MatchResponse update(Long id, MatchRequest req) {
        validateOddsSpecifiers(req.getOdds());

        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        mapper.updateEntity(match, req);

        // PUT semantics for nested odds:
        // - odds == null -> leave unchanged
        // - odds != null -> replace all
        if (req.getOdds() != null) {
            match.getOdds().clear();
            matchRepository.flush();

            for (MatchOddsRequest o : req.getOdds()) {
                MatchOdds odd = mapper.toEntity(o);
                odd.setMatch(match);
                match.getOdds().add(odd);
            }

            matchRepository.flush();
        }

        return mapper.toResponse(match, true);
    }

    public void delete(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException("Match not found: " + id));
        matchRepository.delete(match);
    }

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
}