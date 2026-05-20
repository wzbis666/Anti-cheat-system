package com.anticheat.backend.service;

import com.anticheat.backend.model.Player;
import com.anticheat.backend.model.Punishment;
import com.anticheat.backend.repository.PlayerRepository;
import com.anticheat.backend.repository.PunishmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PunishmentServiceTest {

    @Mock
    private PunishmentRepository punishmentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PunishmentService punishmentService;

    private Player player;
    private Punishment activePermBan;
    private Punishment activeTempBan;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setPlayerName("Cheater");
        player.setUuid("cheater-uuid");
        player.setRiskScore(50);

        activePermBan = new Punishment();
        activePermBan.setId(1L);
        activePermBan.setPlayer(player);
        activePermBan.setPunishmentType("PERMANENT");
        activePermBan.setPunishmentTime(System.currentTimeMillis());
        activePermBan.setActive(true);
        activePermBan.setReason("Aimbot detected");

        activeTempBan = new Punishment();
        activeTempBan.setId(2L);
        activeTempBan.setPlayer(player);
        activeTempBan.setPunishmentType("TEMPORARY");
        activeTempBan.setPunishmentTime(System.currentTimeMillis());
        activeTempBan.setDuration(3600000L);
        activeTempBan.setActive(true);
    }

    @Test
    void banPlayer_shouldCreateNewBan() {
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid"))
                .thenReturn(new ArrayList<>());
        when(playerRepository.findByUuid("cheater-uuid")).thenReturn(Optional.empty());
        when(playerRepository.findByPlayerName("Cheater")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(punishmentRepository.save(any(Punishment.class))).thenAnswer(inv -> {
            Punishment p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        Punishment result = punishmentService.banPlayer("Cheater", "cheater-uuid",
                "PERMANENT", 0, "Aimbot detected");

        assertNotNull(result);
        assertEquals("PERMANENT", result.getPunishmentType());
        assertTrue(result.isActive());
        assertEquals(player, result.getPlayer());
    }

    @Test
    void banPlayer_shouldSkipWhenExistingPermBanActive() {
        List<Punishment> activeBans = new ArrayList<>();
        activeBans.add(activePermBan);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid"))
                .thenReturn(activeBans);

        Punishment result = punishmentService.banPlayer("Cheater", "cheater-uuid",
                "TEMPORARY", 3600000, "Another reason");

        assertNotNull(result);
        assertEquals("PERMANENT", result.getPunishmentType());
        verify(punishmentRepository, never()).save(any(Punishment.class));
    }

    @Test
    void banPlayer_shouldUpgradeTempBanToPermanent() {
        List<Punishment> activeBans = new ArrayList<>();
        activeBans.add(activeTempBan);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid"))
                .thenReturn(activeBans);

        when(playerRepository.findByUuid("cheater-uuid")).thenReturn(Optional.of(player));
        when(punishmentRepository.save(any(Punishment.class))).thenAnswer(inv -> inv.getArgument(0));

        Punishment result = punishmentService.banPlayer("Cheater", "cheater-uuid",
                "PERMANENT", 0, "Upgraded");

        verify(punishmentRepository).save(activeTempBan);
        assertFalse(activeTempBan.isActive());
        assertEquals("SYSTEM_UPGRADE", activeTempBan.getUnbannedBy());
        assertNotNull(result);
    }

    @Test
    void isPlayerBanned_shouldReturnTrueForActivePermBan() {
        List<Punishment> activeBans = new ArrayList<>();
        activeBans.add(activePermBan);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid")).thenReturn(activeBans);

        assertTrue(punishmentService.isPlayerBanned("cheater-uuid"));
    }

    @Test
    void isPlayerBanned_shouldReturnTrueForActiveTempBan() {
        List<Punishment> activeBans = new ArrayList<>();
        activeBans.add(activeTempBan);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid")).thenReturn(activeBans);

        assertTrue(punishmentService.isPlayerBanned("cheater-uuid"));
    }

    @Test
    void isPlayerBanned_shouldReturnFalseForExpiredTempBan() {
        Punishment expired = new Punishment();
        expired.setId(3L);
        expired.setPlayer(player);
        expired.setPunishmentType("TEMPORARY");
        expired.setPunishmentTime(System.currentTimeMillis() - 7200000L);
        expired.setDuration(3600000L);
        expired.setActive(true);

        List<Punishment> bans = new ArrayList<>();
        bans.add(expired);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid")).thenReturn(bans);

        assertFalse(punishmentService.isPlayerBanned("cheater-uuid"));
        verify(punishmentRepository).save(expired);
    }

    @Test
    void isPlayerBanned_shouldReturnFalseWhenNoBans() {
        when(punishmentRepository.findActiveBansByUuid("unknown")).thenReturn(new ArrayList<>());

        assertFalse(punishmentService.isPlayerBanned("unknown"));
    }

    @Test
    void unbanPlayer_shouldDeactivateAndResetKickCount() {
        when(punishmentRepository.findById(1L)).thenReturn(Optional.of(activePermBan));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(punishmentRepository.save(any(Punishment.class))).thenReturn(activePermBan);

        Punishment result = punishmentService.unbanPlayer(1L, "Admin");

        assertNotNull(result);
        assertFalse(result.isActive());
        assertEquals("Admin", result.getUnbannedBy());
        verify(playerRepository).save(player);
        assertEquals(0, player.getKickCount());
    }

    @Test
    void unbanPlayer_shouldReturnNullWhenNotFound() {
        when(punishmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertNull(punishmentService.unbanPlayer(999L, "Admin"));
    }

    @Test
    void getActiveBan_shouldReturnPermanentOverTemporary() {
        List<Punishment> activeBans = new ArrayList<>();
        activeBans.add(activePermBan);
        activeBans.add(activeTempBan);
        when(punishmentRepository.findActiveBansByUuid("cheater-uuid")).thenReturn(activeBans);

        Punishment result = punishmentService.getActiveBan("cheater-uuid");

        assertNotNull(result);
        assertEquals("PERMANENT", result.getPunishmentType());
    }
}
