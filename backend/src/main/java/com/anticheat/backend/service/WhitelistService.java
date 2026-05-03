package com.anticheat.backend.service;

import com.anticheat.backend.model.Whitelist;
import com.anticheat.backend.repository.WhitelistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WhitelistService {

    private static final Logger logger = LoggerFactory.getLogger(WhitelistService.class);

    @Autowired
    private WhitelistRepository whitelistRepository;

    public List<Whitelist> getAll() {
        return whitelistRepository.findAll();
    }

    public List<Whitelist> getActive() {
        return whitelistRepository.findByActiveTrue();
    }

    public Optional<Whitelist> getByUuid(String uuid) {
        return whitelistRepository.findByUuid(uuid);
    }

    public boolean isWhitelisted(String uuid) {
        return whitelistRepository.existsByUuidAndActiveTrue(uuid);
    }

    @Transactional
    public Whitelist add(String playerName, String uuid, String reason, String addedBy) {
        Optional<Whitelist> existing = whitelistRepository.findByUuid(uuid);
        
        if (existing.isPresent()) {
            Whitelist whitelist = existing.get();
            whitelist.setActive(true);
            whitelist.setPlayerName(playerName);
            whitelist.setReason(reason);
            whitelist.setAddedBy(addedBy);
            whitelist.setAddedTime(System.currentTimeMillis());
            whitelistRepository.save(whitelist);
            logger.info("更新白名单: {} ({})", playerName, uuid);
            return whitelist;
        }
        
        Whitelist whitelist = new Whitelist();
        whitelist.setPlayerName(playerName);
        whitelist.setUuid(uuid);
        whitelist.setReason(reason);
        whitelist.setAddedBy(addedBy);
        whitelist.setAddedTime(System.currentTimeMillis());
        whitelist.setActive(true);
        whitelistRepository.save(whitelist);
        logger.info("添加白名单: {} ({})", playerName, uuid);
        return whitelist;
    }

    @Transactional
    public void remove(String uuid) {
        Optional<Whitelist> whitelist = whitelistRepository.findByUuid(uuid);
        if (whitelist.isPresent()) {
            Whitelist w = whitelist.get();
            w.setActive(false);
            whitelistRepository.save(w);
            logger.info("移除白名单: {} ({})", w.getPlayerName(), uuid);
        }
    }

    @Transactional
    public void delete(Long id) {
        whitelistRepository.deleteById(id);
        logger.info("删除白名单记录: {}", id);
    }
}
