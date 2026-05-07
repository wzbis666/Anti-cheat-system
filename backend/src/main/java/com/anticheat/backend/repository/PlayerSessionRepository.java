package com.anticheat.backend.repository;

import com.anticheat.backend.model.PlayerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {

    List<PlayerSession> findByPlayerIdOrderByLoginTimeDesc(Long playerId);

    @Query("SELECT s.ipAddress FROM PlayerSession s WHERE s.player.id = ?1 GROUP BY s.ipAddress")
    List<String> findDistinctIpsByPlayerId(Long playerId);

    @Query("SELECT s.player.id FROM PlayerSession s WHERE s.ipAddress = ?1 AND s.player.id != ?2 GROUP BY s.player.id")
    List<Long> findOtherPlayersOnSameIp(String ipAddress, Long playerId);
}
