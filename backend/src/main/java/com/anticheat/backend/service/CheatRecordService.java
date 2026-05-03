package com.anticheat.backend.service;

import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.model.Player;
import com.anticheat.backend.repository.CheatRecordRepository;
import com.anticheat.backend.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CheatRecordService {

    private static final Logger logger = LoggerFactory.getLogger(CheatRecordService.class);

    private final CheatRecordRepository cheatRecordRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    public CheatRecordService(CheatRecordRepository cheatRecordRepository) {
        this.cheatRecordRepository = cheatRecordRepository;
    }

    public CheatRecord saveCheatRecord(CheatRecord record) {
        CheatRecord saved = cheatRecordRepository.save(record);
        logger.info("保存作弊记录: 玩家={}, 类型={}, 严重程度={}",
                    record.getPlayer().getPlayerName(),
                    record.getCheatType(),
                    record.getSeverity());
        return saved;
    }

    @Transactional
    public CheatRecord createCheatRecord(String playerName, String uuid, String cheatType, int severity, String details) {
        Player player = playerRepository.findByUuid(uuid)
                .orElseGet(() -> {
                    Player p = new Player();
                    p.setPlayerName(playerName != null ? playerName : "Unknown");
                    p.setUuid(uuid);
                    p.setRiskScore(0);
                    p.setLastSeen(System.currentTimeMillis());
                    return playerRepository.save(p);
                });

        CheatRecord record = new CheatRecord();
        record.setPlayer(player);
        record.setCheatType(cheatType);
        record.setSeverity(severity);
        record.setDetectionTime(System.currentTimeMillis());
        record.setDetails(details);
        CheatRecord saved = cheatRecordRepository.save(record);

        player.setRiskScore(player.getRiskScore() + severity);
        playerRepository.save(player);

        logger.info("创建作弊记录: 玩家={}, 类型={}, 严重程度={}", playerName, cheatType, severity);
        return saved;
    }

    public List<CheatRecord> getAllCheatRecords() {
        return cheatRecordRepository.findAll();
    }

    public Page<CheatRecord> getCheatRecordsByPage(Pageable pageable) {
        return cheatRecordRepository.findAll(pageable);
    }

    public Page<CheatRecord> getCheatRecordsByType(String cheatType, Pageable pageable) {
        return cheatRecordRepository.findByCheatType(cheatType, pageable);
    }

    public List<CheatRecord> getCheatRecordsByPlayerUuid(String uuid) {
        return cheatRecordRepository.findByPlayerUuid(uuid);
    }

    public long getTotalCheats() {
        return cheatRecordRepository.count();
    }

    public Map<String, Integer> getCheatTypeStatistics() {
        List<Object[]> results = cheatRecordRepository.countByCheatType();
        Map<String, Integer> stats = new HashMap<>();
        for (Object[] row : results) {
            stats.put((String) row[0], ((Number) row[1]).intValue());
        }
        return stats;
    }

    public long getRecentCheatsCount(long startTime) {
        return cheatRecordRepository.countByDetectionTimeAfter(startTime);
    }

    public void deleteCheatRecord(Long id) {
        cheatRecordRepository.deleteById(id);
        logger.info("删除作弊记录 ID: {}", id);
    }
}
