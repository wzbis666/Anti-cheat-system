package com.anticheat.backend.repository;

import com.anticheat.backend.model.Whitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Long> {
    
    Optional<Whitelist> findByUuid(String uuid);
    
    List<Whitelist> findByActiveTrue();
    
    @Query("SELECT w FROM Whitelist w WHERE w.uuid = :uuid AND w.active = true")
    Optional<Whitelist> findActiveByUuid(@Param("uuid") String uuid);
    
    boolean existsByUuidAndActiveTrue(String uuid);
}
