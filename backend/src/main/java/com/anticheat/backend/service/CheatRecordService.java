package com.anticheat.backend.service;

import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.model.Player;
import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.repository.CheatRecordRepository;
import com.anticheat.backend.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private PunishmentService punishmentService;

    @Autowired
    private SystemSettingsService settingsService;

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
        return createCheatRecord(playerName, uuid, cheatType, severity, details, "AUTO_DETECT", null, null);
    }

    @Transactional
    public CheatRecord createCheatRecord(String playerName, String uuid, String cheatType, int severity,
                                          String details, String detectionMethod, String evidence, String serverName) {
        Player player = playerRepository.findByUuid(uuid)
                .orElseGet(() -> {
                    Player p = new Player();
                    p.setPlayerName(playerName != null ? playerName : "Unknown");
                    p.setUuid(uuid);
                    p.setRiskScore(0);
                    p.setLastSeen(System.currentTimeMillis());
                    p.setFirstSeen(System.currentTimeMillis());
                    return playerRepository.save(p);
                });

        CheatRecord record = new CheatRecord();
        record.setPlayer(player);
        record.setCheatType(cheatType);
        record.setSeverity(severity);
        record.setDetectionTime(System.currentTimeMillis());
        record.setDetails(details);
        record.setDetectionMethod(detectionMethod != null ? detectionMethod : "AUTO_DETECT");
        record.setEvidence(evidence);
        record.setServerName(serverName);
        CheatRecord saved = cheatRecordRepository.save(record);

        player.setRiskScore(player.getRiskScore() + severity);
        playerRepository.save(player);

        // Progressive punishment check
        if (settingsService.getBooleanSetting("progressive_punishment_enabled", true)) {
            checkProgressivePunishment(player, cheatType);
        }

        logger.info("创建作弊记录: 玩家={}, 类型={}, 严重程度={}", playerName, cheatType, severity);
        return saved;
    }

    private void checkProgressivePunishment(Player player, String cheatType) {
        String typeKey = cheatType.toLowerCase().replaceAll("[^a-z]", "");
        int warnThreshold = settingsService.getIntSetting("cheat." + typeKey + ".warn_threshold", 3);
        int kickThreshold = settingsService.getIntSetting("cheat." + typeKey + ".kick_threshold", 5);
        int tempBanThreshold = settingsService.getIntSetting("cheat." + typeKey + ".temp_ban_threshold", 8);
        int permBanThreshold = settingsService.getIntSetting("cheat." + typeKey + ".perm_ban_threshold", 12);

        List<CheatRecord> records = cheatRecordRepository.findByPlayerUuid(player.getUuid());
        int count = records.size();

        if (count >= permBanThreshold) {
            punishmentService.banPlayer(player.getPlayerName(), player.getUuid(),
                    "PERMANENT", 0, "自动封禁: " + cheatType + " 达到阈值 " + permBanThreshold);
            logger.warn("玩家 {} 达到永久封禁阈值，已自动封禁", player.getPlayerName());
        } else if (count >= tempBanThreshold) {
            long duration = 7 * 24 * 60 * 60 * 1000L;
            punishmentService.banPlayer(player.getPlayerName(), player.getUuid(),
                    "TEMPORARY", duration, "自动临时封禁: " + cheatType + " 达到阈值 " + tempBanThreshold);
            logger.warn("玩家 {} 达到临时封禁阈值，已自动临时封禁", player.getPlayerName());
        } else if (count >= kickThreshold) {
            player.incrementKickCount();
            playerRepository.save(player);
            logger.warn("玩家 {} 达到踢出阈值，踢出次数: {}", player.getPlayerName(), player.getKickCount());
        } else if (count >= warnThreshold) {
            logger.info("玩家 {} 达到警告阈值，作弊记录数: {}", player.getPlayerName(), count);
        }
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

    public List<Map<String, Object>> getHourlyBreakdown(long startTime) {
        List<Object[]> results = cheatRecordRepository.countByHour(startTime);
        List<Map<String, Object>> hourlyData = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> point = new HashMap<>();
            point.put("hour", row[0]);
            point.put("count", ((Number) row[1]).intValue());
            hourlyData.add(point);
        }
        return hourlyData;
    }
}
