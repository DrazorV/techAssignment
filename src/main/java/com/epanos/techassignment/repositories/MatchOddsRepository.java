package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.MatchOdds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchOddsRepository extends JpaRepository<MatchOdds, Long> {

    /**
     * Retrieves all non-paginated odds for a given match.
     *
     * @param matchId the match ID
     * @return list of odds for the match
     */
    List<MatchOdds> findByMatchId(Long matchId);

    /**
     * Retrieves a paginated list of odds for a given match.
     *
     * @param matchId the match ID
     * @param pageable the pagination parameters (page, size, sort)
     * @return a page of odds for the match
     */
    Page<MatchOdds> findByMatchId(Long matchId, Pageable pageable);

    /**
     * Retrieves a specific odd by ID and match ID.
     *
     * @param id the odd ID
     * @param matchId the match ID
     * @return optional containing the odd if found
     */
    Optional<MatchOdds> findByIdAndMatchId(Long id, Long matchId);

    /**
     * Retrieves a specific odd by match ID and specifier.
     *
     * @param matchId the match ID
     * @param specifier the odd specifier
     * @return optional containing the odd if found
     */
    Optional<MatchOdds> findByMatchIdAndSpecifier(Long matchId, String specifier);

    /**
     * Checks if an odd with the given specifier exists for a match.
     *
     * @param matchId the match ID
     * @param specifier the odd specifier
     * @return true if exists, false otherwise
     */
    boolean existsByMatchIdAndSpecifier(Long matchId, String specifier);

    /**
     * Checks if an odd with the given specifier exists for a match, excluding a specific odd ID.
     * Used during updates to allow the current odd to keep its own specifier.
     *
     * @param matchId the match ID
     * @param specifier the odd specifier
     * @param id the odd ID to exclude from the check
     * @return true if another odd with the same specifier exists, false otherwise
     */
    boolean existsByMatchIdAndSpecifierAndIdNot(Long matchId, String specifier, Long id);
}
