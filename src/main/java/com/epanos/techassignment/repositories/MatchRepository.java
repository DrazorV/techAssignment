package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    /**
     * Retrieves all matches with associated odds (non-paginated).
     *
     * @return list of all matches with odds
     */
    @Query("select distinct m from Match m left join fetch m.odds")
    List<Match> findAllWithOdds();

    /**
     * Retrieves a paginated list of matches with associated odds.
     *
     * @param pageable the pagination parameters (page, size, sort)
     * @return a page of matches with odds included
     */
    @Query("select distinct m from Match m left join fetch m.odds")
    Page<Match> findAllWithOdds(Pageable pageable);
}