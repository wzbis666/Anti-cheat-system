package com.anticheat.backend.service;

import com.anticheat.backend.model.AuditLog;
import com.anticheat.backend.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog log(Long adminId, String adminName, String actionType, String targetType,
                        Long targetId, String targetName, String detail) {
        AuditLog log = new AuditLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setDetail(detail);
        AuditLog saved = auditLogRepository.save(log);
        logger.info("审计日志: {} {} {} {} {}", adminName, actionType, targetType, targetName, detail);
        return saved;
    }

    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    public List<AuditLog> getByActionType(String actionType) {
        return auditLogRepository.findByActionTypeOrderByCreateTimeDesc(actionType);
    }

    public List<AuditLog> getByAdminId(Long adminId) {
        return auditLogRepository.findByAdminIdOrderByCreateTimeDesc(adminId);
    }

    @Transactional
    public void deleteOlderThan(long timestamp) {
        List<AuditLog> oldLogs = auditLogRepository.findByCreateTimeAfterOrderByCreateTimeDesc(timestamp);
        // Delete logs older than the given timestamp — find all and remove old ones
        List<AuditLog> allLogs = auditLogRepository.findAll();
        int deleted = 0;
        for (AuditLog log : allLogs) {
            if (log.getCreateTime() < timestamp) {
                auditLogRepository.delete(log);
                deleted++;
            }
        }
        if (deleted > 0) {
            logger.info("清理 {} 条过期审计日志", deleted);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void scheduledCleanup() {
        long ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
        deleteOlderThan(ninetyDaysAgo);
    }
}
