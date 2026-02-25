package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * @return a page of match IDs
     */
    @Query(value = "select m.id from Match m",
           countQuery = "select count(m) from Match m")
    Page<Long> findAllIds(Pageable pageable);

    /**
     * Retrieves matches with associated odds for a given list of IDs.
     * Used as the second step in the two-query pagination approach.
     *
     * @param ids the list of match IDs to fetch
     * @return list of matches with odds eagerly loaded
     */
    @Query("select distinct m from Match m left join fetch m.odds where m.id in :ids")
    List<Match> findAllWithOddsByIds(@Param("ids") List<Long> ids);
}