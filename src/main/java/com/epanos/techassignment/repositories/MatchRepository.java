package com.epanos.techassignment.repositories;

import com.epanos.techassignment.models.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {}