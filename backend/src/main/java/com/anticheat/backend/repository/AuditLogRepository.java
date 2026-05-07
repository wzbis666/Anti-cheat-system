package com.anticheat.backend.repository;

import com.anticheat.backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreateTimeDesc(Pageable pageable);

    List<AuditLog> findByActionTypeOrderByCreateTimeDesc(String actionType);

    List<AuditLog> findByAdminIdOrderByCreateTimeDesc(Long adminId);

    List<AuditLog> findByCreateTimeAfterOrderByCreateTimeDesc(long since);
}
