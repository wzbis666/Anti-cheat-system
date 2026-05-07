package com.anticheat.backend.service;

import com.anticheat.backend.model.Appeal;
import com.anticheat.backend.repository.AppealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppealService {

    private static final Logger logger = LoggerFactory.getLogger(AppealService.class);

    @Autowired
    private AppealRepository appealRepository;

    @Transactional
    public Appeal create(String playerName, String playerUuid, Long punishmentId, String reason) {
        Appeal appeal = new Appeal();
        appeal.setPlayerName(playerName);
        appeal.setPlayerUuid(playerUuid);
        appeal.setPunishmentId(punishmentId);
        appeal.setReason(reason);
        appeal.setStatus("PENDING");
        Appeal saved = appealRepository.save(appeal);
        logger.info("新申诉: {} 对处罚 {} 提出申诉", playerName, punishmentId);
        return saved;
    }

    public List<Appeal> getAll() {
        return appealRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<Appeal> getPending() {
        return appealRepository.findByStatusOrderByCreateTimeDesc("PENDING");
    }

    public List<Appeal> getByPlayerUuid(String uuid) {
        return appealRepository.findByPlayerUuidOrderByCreateTimeDesc(uuid);
    }

    public Optional<Appeal> getById(Long id) {
        return appealRepository.findById(id);
    }

    public long getPendingCount() {
        return appealRepository.countByStatus("PENDING");
    }

    @Transactional
    public Appeal handle(Long id, String handledBy, String status, String adminResponse) {
        Optional<Appeal> optional = appealRepository.findById(id);
        if (optional.isPresent()) {
            Appeal appeal = optional.get();
            appeal.setStatus(status);
            appeal.setHandledBy(handledBy);
            appeal.setAdminResponse(adminResponse);
            appeal.setHandleTime(System.currentTimeMillis());
            Appeal saved = appealRepository.save(appeal);
            logger.info("申诉已处理: ID={}, 状态={}, 处理人={}", id, status, handledBy);
            return saved;
        }
        return null;
    }

    @Transactional
    public void delete(Long id) {
        appealRepository.deleteById(id);
    }
}
