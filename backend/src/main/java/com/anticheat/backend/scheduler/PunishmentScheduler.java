package com.anticheat.backend.scheduler;

import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.repository.PunishmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PunishmentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PunishmentScheduler.class);

    @Autowired
    private PunishmentRepository punishmentRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredPunishments() {
        long currentTime = System.currentTimeMillis();
        
        List<Punishment> expiredPunishments = punishmentRepository.findExpiredTemporaryBans(currentTime);
        
        for (Punishment punishment : expiredPunishments) {
            punishment.setActive(false);
            punishment.setUnbannedTime(currentTime);
            punishment.setUnbannedBy("SYSTEM_AUTO");
            punishmentRepository.save(punishment);
            
            String playerName = punishment.getPlayer() != null ? punishment.getPlayer().getPlayerName() : "Unknown";
            logger.info("临时封禁已自动解除: 玩家={}, 封禁ID={}", playerName, punishment.getId());
        }
        
        if (!expiredPunishments.isEmpty()) {
            logger.info("本次检查共解除 {} 个过期封禁", expiredPunishments.size());
        }
    }
}
