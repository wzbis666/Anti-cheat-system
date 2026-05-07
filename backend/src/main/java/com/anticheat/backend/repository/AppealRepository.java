package com.anticheat.backend.repository;

import com.anticheat.backend.model.Appeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

    List<Appeal> findAllByOrderByCreateTimeDesc();

    List<Appeal> findByStatusOrderByCreateTimeDesc(String status);

    List<Appeal> findByPlayerUuidOrderByCreateTimeDesc(String playerUuid);

    long countByStatus(String status);
}
