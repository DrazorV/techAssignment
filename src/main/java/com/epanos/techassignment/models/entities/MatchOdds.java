package com.epanos.techassignment.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "match_odds",
        uniqueConstraints = { @UniqueConstraint(name = "uk_match_specifier", columnNames = {"match_id", "specifier"})},
        indexes = {@Index(name = "idx_match_id", columnList = "match_id")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchOdds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Match match;

    @Column(nullable = false, length = 16)
    private String specifier;

    @Digits(integer = 3, fraction = 3)
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal odd;
}