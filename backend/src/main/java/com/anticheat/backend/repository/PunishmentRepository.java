package com.anticheat.backend.repository;

import com.anticheat.backend.model.Punishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PunishmentRepository extends JpaRepository<Punishment, Long> {

    List<Punishment> findByPlayerId(Long playerId);

    @Query("SELECT p FROM Punishment p WHERE p.player.uuid = :uuid")
    List<Punishment> findByPlayerUuid(@Param("uuid") String uuid);

    List<Punishment> findByActiveTrue();

    long countByActiveTrue();

    long count();

    List<Punishment> findByPlayerUuidOrderByPunishmentTimeDesc(@Param("uuid") String uuid);

    @Query("SELECT p FROM Punishment p WHERE p.player.uuid = :uuid AND p.active = true AND (p.punishmentType = 'PERMANENT' OR (p.punishmentType = 'TEMPORARY' AND (p.punishmentTime + p.duration) > :currentTime))")
    List<Punishment> findActiveBansByUuid(@Param("uuid") String uuid, @Param("currentTime") long currentTime);

    @Query("SELECT p FROM Punishment p WHERE p.player.uuid = :uuid AND p.active = true")
    List<Punishment> findActiveBansByUuid(@Param("uuid") String uuid);

    @Query("SELECT p FROM Punishment p WHERE p.active = true AND p.punishmentType = 'TEMPORARY' AND (p.punishmentTime + p.duration) <= :currentTime")
    List<Punishment> findExpiredTemporaryBans(@Param("currentTime") long currentTime);

    @Query("SELECT p FROM Punishment p ORDER BY p.punishmentTime DESC")
    Page<Punishment> findAllPaged(Pageable pageable);

    @Query("SELECT p FROM Punishment p WHERE p.active = true ORDER BY p.punishmentTime DESC")
    Page<Punishment> findActivePaged(Pageable pageable);
}
