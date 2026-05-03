package com.anticheat.backend.repository;

import com.anticheat.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUuid(String uuid);

    Optional<Player> findByPlayerName(String playerName);

    List<Player> findByRiskScoreGreaterThan(int riskScore);

    List<Player> findByRiskScoreBetween(int minScore, int maxScore);

    @Query("SELECT COUNT(p) FROM Player p")
    long countTotalPlayers();

    @Query("SELECT COUNT(p) FROM Player p WHERE p.riskScore >= :threshold")
    long countHighRiskPlayers(@Param("threshold") int threshold);

    @Query("SELECT COUNT(p) FROM Player p WHERE p.riskScore > :min AND p.riskScore < :max")
    long countMediumRiskPlayers(@Param("min") int min, @Param("max") int max);
}
