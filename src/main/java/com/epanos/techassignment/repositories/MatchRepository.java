package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    @Query("select distinct m from Match m left join fetch m.odds")
    List<Match> findAllWithOdds();
}