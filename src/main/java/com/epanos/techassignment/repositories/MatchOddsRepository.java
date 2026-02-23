package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.MatchOdds;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchOddsRepository extends JpaRepository<MatchOdds, Long> {

    List<MatchOdds> findByMatchId(Long matchId);

    Optional<MatchOdds> findByIdAndMatchId(Long id, Long matchId);

    boolean existsByMatchIdAndSpecifier(Long matchId, String specifier);
}
