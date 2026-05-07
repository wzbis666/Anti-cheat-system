package com.anticheat.backend.service;

import com.anticheat.backend.model.Player;
import com.anticheat.backend.model.PlayerSession;
import com.anticheat.backend.repository.PlayerRepository;
import com.anticheat.backend.repository.PlayerSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;

    @Autowired
    private PlayerSessionRepository sessionRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player getOrCreatePlayer(String playerName, String uuid, String ipAddress) {
        Optional<Player> existingPlayer = playerRepository.findByUuid(uuid);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setLastSeen(System.currentTimeMillis());
            if (ipAddress != null) player.setLastIp(ipAddress);
            if (!player.getPlayerName().equals(playerName)) {
                player.setPlayerName(playerName);
            }
            return playerRepository.save(player);
        }
        Player player = getOrCreatePlayer(playerName, uuid);
        if (ipAddress != null) player.setLastIp(ipAddress);
        return playerRepository.save(player);
    }

    public Player getOrCreatePlayer(String playerName, String uuid) {
        Optional<Player> existingPlayer = playerRepository.findByUuid(uuid);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setLastSeen(System.currentTimeMillis());
            if (!player.getPlayerName().equals(playerName)) {
                player.setPlayerName(playerName);
                playerRepository.save(player);
            }
            return player;
        }
        
        Optional<Player> existingByName = playerRepository.findByPlayerName(playerName);
        if (existingByName.isPresent()) {
            Player player = existingByName.get();
            player.setUuid(uuid);
            player.setLastSeen(System.currentTimeMillis());
            playerRepository.save(player);
            return player;
        }
        
        Player newPlayer = new Player();
        newPlayer.setPlayerName(playerName);
        newPlayer.setUuid(uuid);
        newPlayer.setRiskScore(0);
        newPlayer.setLastSeen(System.currentTimeMillis());
        logger.info("创建新玩家: {} ({})", playerName, uuid);
        return playerRepository.save(newPlayer);
    }

    public Optional<Player> findByUuid(String uuid) {
        return playerRepository.findByUuid(uuid);
    }

    public Optional<Player> findByPlayerName(String playerName) {
        return playerRepository.findByPlayerName(playerName);
    }

    public Player updatePlayer(Player player) {
        player.setLastSeen(System.currentTimeMillis());
        return playerRepository.save(player);
    }

    public Player updateRiskScore(Player player, int additionalScore) {
        player.setRiskScore(player.getRiskScore() + additionalScore);
        logger.info("更新玩家 {} 风险评分: {} -> {}", player.getPlayerName(),
                    player.getRiskScore() - additionalScore, player.getRiskScore());
        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public List<Player> getHighRiskPlayers(int threshold) {
        return playerRepository.findByRiskScoreGreaterThan(threshold);
    }

    public long getTotalPlayers() {
        return playerRepository.count();
    }

    public long getHighRiskPlayerCount(int threshold) {
        return playerRepository.countHighRiskPlayers(threshold);
    }

    public void deletePlayer(Long id) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("玩家不存在: " + id));
        player.getCheatRecords().size();
        player.getPunishments().size();
        playerRepository.delete(player);
        logger.info("删除玩家 ID: {}, 作弊记录: {}, 处罚记录: {}",
            id, player.getCheatRecords().size(), player.getPunishments().size());
    }

    public int incrementKickCount(String uuid) {
        Optional<Player> optionalPlayer = playerRepository.findByUuid(uuid);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            int newCount = player.getKickCount() + 1;
            player.setKickCount(newCount);
            playerRepository.save(player);
            logger.info("玩家 {} 踢出次数更新: {}", player.getPlayerName(), newCount);
            return newCount;
        }
        return 0;
    }

    public int incrementKickCount(String playerName, String uuid) {
        Player player = getOrCreatePlayer(playerName, uuid);
        int currentKickCount = player.getKickCount();
        int newCount = currentKickCount + 1;
        player.setKickCount(newCount);
        Player saved = playerRepository.save(player);
        logger.info("玩家 {} 踢出次数更新: {} -> {} (保存后: {})", 
            player.getPlayerName(), currentKickCount, newCount, saved.getKickCount());
        return saved.getKickCount();
    }

    public int getKickCount(String uuid) {
        Optional<Player> optionalPlayer = playerRepository.findByUuid(uuid);
        return optionalPlayer.map(Player::getKickCount).orElse(0);
    }

    public void resetKickCount(String uuid) {
        Optional<Player> optionalPlayer = playerRepository.findByUuid(uuid);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.setKickCount(0);
            playerRepository.save(player);
            logger.info("玩家 {} 踢出次数已重置", player.getPlayerName());
        }
    }

    public void updateRiskScore(Long id, int score) {
        Optional<Player> optionalPlayer = playerRepository.findById(id);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            int oldScore = player.getRiskScore();
            player.setRiskScore(score);
            playerRepository.save(player);
            logger.info("更新玩家 {} 风险评分: {} -> {}", player.getPlayerName(), oldScore, score);
        } else {
            throw new RuntimeException("玩家不存在: " + id);
        }
    }

    @Transactional
    public PlayerSession recordLogin(String playerName, String uuid, String ipAddress, String serverName) {
        Player player = getOrCreatePlayer(playerName, uuid, ipAddress);
        PlayerSession session = new PlayerSession();
        session.setPlayer(player);
        session.setIpAddress(ipAddress);
        session.setLoginTime(System.currentTimeMillis());
        session.setServerName(serverName);
        logger.info("记录玩家登录: {} IP={}", playerName, ipAddress);
        return sessionRepository.save(session);
    }

    @Transactional
    public void recordLogout(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLogoutTime(System.currentTimeMillis());
            session.setDuration(session.getLogoutTime() - session.getLoginTime());
            sessionRepository.save(session);
        });
    }

    public List<PlayerSession> getPlayerSessions(Long playerId) {
        return sessionRepository.findByPlayerIdOrderByLoginTimeDesc(playerId);
    }

    public List<String> getPlayerDistinctIps(Long playerId) {
        return sessionRepository.findDistinctIpsByPlayerId(playerId);
    }

    public List<Long> getOtherPlayersOnSameIp(String ipAddress, Long playerId) {
        return sessionRepository.findOtherPlayersOnSameIp(ipAddress, playerId);
    }
}
