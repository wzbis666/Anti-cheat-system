package com.anticheat.backend.service;

import com.anticheat.backend.model.Player;
import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.repository.PlayerRepository;
import com.anticheat.backend.repository.PunishmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PunishmentService {

    private static final Logger logger = LoggerFactory.getLogger(PunishmentService.class);

    @Autowired
    private PunishmentRepository punishmentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public List<Punishment> getAllPunishments() {
        return punishmentRepository.findAll();
    }

    public List<Punishment> getActivePunishments() {
        return punishmentRepository.findByActiveTrue();
    }

    public List<Punishment> getPunishmentsByPlayerId(Long playerId) {
        return punishmentRepository.findByPlayerId(playerId);
    }

    public List<Punishment> getPunishmentsByPlayerUuid(String uuid) {
        return punishmentRepository.findByPlayerUuid(uuid);
    }

    public Optional<Punishment> getPunishmentById(Long id) {
        return punishmentRepository.findById(id);
    }

    @Transactional
    public Punishment banPlayer(String playerName, String uuid, String punishmentType, 
                                 long duration, String reason) {
        logger.info("========== 开始封禁玩家 ==========");
        logger.info("玩家名: {}, UUID: {}", playerName, uuid);
        logger.info("封禁类型: {}, 时长: {}, 原因: {}", punishmentType, duration, reason);
        
        Player player = playerRepository.findByUuid(uuid)
            .orElseGet(() -> {
                Optional<Player> existingByName = playerRepository.findByPlayerName(playerName);
                if (existingByName.isPresent()) {
                    logger.info("通过玩家名找到已有记录，更新UUID: {} -> {}", existingByName.get().getUuid(), uuid);
                    Player existing = existingByName.get();
                    existing.setUuid(uuid);
                    existing.setLastSeen(System.currentTimeMillis());
                    return playerRepository.save(existing);
                }
                logger.info("玩家不存在，创建新玩家记录");
                Player newPlayer = new Player();
                newPlayer.setPlayerName(playerName);
                newPlayer.setUuid(uuid);
                newPlayer.setRiskScore(0);
                newPlayer.setLastSeen(System.currentTimeMillis());
                return playerRepository.save(newPlayer);
            });
        
        logger.info("玩家ID: {}", player.getId());

        Punishment punishment = new Punishment();
        punishment.setPlayer(player);
        punishment.setPunishmentType(punishmentType);
        punishment.setPunishmentTime(System.currentTimeMillis());
        punishment.setDuration(duration);
        punishment.setReason(reason);
        punishment.setActive(true);
        
        logger.info("创建封禁记录...");

        Punishment saved = punishmentRepository.save(punishment);
        logger.info("封禁记录已保存，ID: {}, 活跃状态: {}", saved.getId(), saved.isActive());
        logger.info("========== 封禁玩家完成 ==========");
        return saved;
    }

    @Transactional
    public Punishment unbanPlayer(Long punishmentId, String unbannedBy) {
        Optional<Punishment> optional = punishmentRepository.findById(punishmentId);
        if (optional.isPresent()) {
            Punishment punishment = optional.get();
            punishment.setActive(false);
            punishment.setUnbannedTime(System.currentTimeMillis());
            punishment.setUnbannedBy(unbannedBy);
            
            Player player = punishment.getPlayer();
            if (player != null) {
                Player managedPlayer = playerRepository.findById(player.getId()).orElse(null);
                if (managedPlayer != null) {
                    managedPlayer.setKickCount(0);
                    playerRepository.save(managedPlayer);
                    logger.info("解封玩家 {} 并重置踢出次数为 0", managedPlayer.getPlayerName());
                }
            }
            
            return punishmentRepository.save(punishment);
        }
        return null;
    }

    @Transactional
    public void deletePunishment(Long id) {
        punishmentRepository.deleteById(id);
    }

    public boolean isPlayerBanned(String uuid) {
        logger.info("检查玩家封禁状态: UUID={}", uuid);
        List<Punishment> activeBans = punishmentRepository.findActiveBansByUuid(uuid);
        logger.info("找到 {} 条活跃封禁记录", activeBans.size());
        
        if (activeBans.isEmpty()) {
            logger.info("无活跃封禁记录，玩家未被封禁");
            return false;
        }

        for (Punishment p : activeBans) {
            logger.info("检查封禁记录: ID={}, 类型={}, 活跃={}", p.getId(), p.getPunishmentType(), p.isActive());
            if ("PERMANENT".equals(p.getPunishmentType())) {
                logger.info("发现永久封禁记录，玩家已被封禁");
                return true;
            }
            if ("TEMPORARY".equals(p.getPunishmentType())) {
                long endTime = p.getPunishmentTime() + p.getDuration();
                if (System.currentTimeMillis() < endTime) {
                    logger.info("发现有效临时封禁记录，玩家已被封禁");
                    return true;
                } else {
                    logger.info("临时封禁已过期，更新状态");
                    p.setActive(false);
                    punishmentRepository.save(p);
                }
            }
        }
        logger.info("无有效封禁记录，玩家未被封禁");
        return false;
    }

    public Punishment getActiveBan(String uuid) {
        List<Punishment> activeBans = punishmentRepository.findActiveBansByUuid(uuid);
        if (activeBans.isEmpty()) {
            return null;
        }

        for (Punishment p : activeBans) {
            if ("PERMANENT".equals(p.getPunishmentType())) {
                return p;
            }
            if ("TEMPORARY".equals(p.getPunishmentType())) {
                long endTime = p.getPunishmentTime() + p.getDuration();
                if (System.currentTimeMillis() < endTime) {
                    return p;
                }
            }
        }
        return null;
    }
}
