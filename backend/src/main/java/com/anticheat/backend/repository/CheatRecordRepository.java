package com.anticheat.backend.repository;

import com.anticheat.backend.model.CheatRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Sort;

import java.util.List;

@Repository
public interface CheatRecordRepository extends JpaRepository<CheatRecord, Long> {

    @Query("SELECT c FROM CheatRecord c WHERE c.player.uuid = :uuid")
    List<CheatRecord> findByPlayerUuid(@Param("uuid") String uuid);

    @Query("SELECT c FROM CheatRecord c WHERE c.player.playerName = :name")
    List<CheatRecord> findByPlayerNameOrderByDetectionTimeDesc(@Param("name") String name);

    Page<CheatRecord> findByCheatType(String cheatType, Pageable pageable);

    List<CheatRecord> findByPlayerId(Long playerId);

    @Query("SELECT c FROM CheatRecord c WHERE c.player.uuid = :uuid ORDER BY c.detectionTime DESC")
    List<CheatRecord> findByPlayerUuidOrderByDetectionTimeDesc(@Param("uuid") String uuid);

    @Query("SELECT COUNT(c) FROM CheatRecord c")
    long countTotalCheats();

    @Query("SELECT c.cheatType, COUNT(c) FROM CheatRecord c GROUP BY c.cheatType")
    List<Object[]> countByCheatType();

    @Query("SELECT COUNT(c) FROM CheatRecord c WHERE c.detectionTime >= :startTime")
    long countByDetectionTimeAfter(@Param("startTime") long startTime);

    List<CheatRecord> findAllByOrderByDetectionTimeDesc();

    default List<CheatRecord> findTop50ByOrderByDetectionTimeDesc() {
        return findAllByOrderByDetectionTimeDesc().stream().limit(50).toList();
    }
}
