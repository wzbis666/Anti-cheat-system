package com.anticheat.backend.service;

import com.anticheat.backend.model.Player;
import com.anticheat.backend.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player existingPlayer;

    @BeforeEach
    void setUp() {
        existingPlayer = new Player();
        existingPlayer.setId(1L);
        existingPlayer.setPlayerName("TestPlayer");
        existingPlayer.setUuid("test-uuid-123");
        existingPlayer.setRiskScore(5);
        existingPlayer.setKickCount(2);
        existingPlayer.setLastSeen(System.currentTimeMillis());
    }

    @Test
    void getOrCreatePlayer_shouldReturnExistingPlayer() {
        when(playerRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(existingPlayer));

        Player result = playerService.getOrCreatePlayer("NewName", "test-uuid-123");

        assertNotNull(result);
        assertEquals("NewName", result.getPlayerName());
        assertEquals("test-uuid-123", result.getUuid());
        verify(playerRepository).findByUuid("test-uuid-123");
    }

    @Test
    void getOrCreatePlayer_shouldCreateNewPlayerWhenNotFound() {
        when(playerRepository.findByUuid("new-uuid")).thenReturn(Optional.empty());
        when(playerRepository.findByPlayerName("NewPlayer")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> {
            Player p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        Player result = playerService.getOrCreatePlayer("NewPlayer", "new-uuid");

        assertNotNull(result);
        assertEquals("NewPlayer", result.getPlayerName());
        assertEquals("new-uuid", result.getUuid());
        assertEquals(0, result.getRiskScore());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void getOrCreatePlayer_shouldUpdateUuidForExistingName() {
        when(playerRepository.findByUuid("new-uuid")).thenReturn(Optional.empty());
        when(playerRepository.findByPlayerName("TestPlayer")).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        Player result = playerService.getOrCreatePlayer("TestPlayer", "new-uuid");

        assertEquals("new-uuid", result.getUuid());
    }

    @Test
    void updateRiskScore_shouldIncreaseScore() {
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        Player result = playerService.updateRiskScore(existingPlayer, 10);

        assertEquals(15, result.getRiskScore());
        verify(playerRepository).save(existingPlayer);
    }

    @Test
    void incrementKickCount_shouldIncrementAndReturnNewCount() {
        when(playerRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        int newCount = playerService.incrementKickCount("TestPlayer", "test-uuid-123");

        assertEquals(3, newCount);
    }

    @Test
    void incrementKickCount_shouldReturnZeroForUnknownPlayer() {
        when(playerRepository.findByUuid("unknown")).thenReturn(Optional.empty());

        int newCount = playerService.incrementKickCount("unknown");

        assertEquals(0, newCount);
    }

    @Test
    void resetKickCount_shouldResetToZero() {
        when(playerRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(existingPlayer));

        playerService.resetKickCount("test-uuid-123");

        assertEquals(0, existingPlayer.getKickCount());
        verify(playerRepository).save(existingPlayer);
    }

    @Test
    void updateRiskScoreById_shouldUpdateWhenPlayerExists() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        playerService.updateRiskScore(1L, 25);

        assertEquals(25, existingPlayer.getRiskScore());
    }

    @Test
    void updateRiskScoreById_shouldThrowWhenPlayerNotFound() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                playerService.updateRiskScore(999L, 10));
    }

    @Test
    void findByUuid_shouldDelegateToRepository() {
        when(playerRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(existingPlayer));

        Optional<Player> result = playerService.findByUuid("test-uuid-123");

        assertTrue(result.isPresent());
        assertEquals("TestPlayer", result.get().getPlayerName());
    }

    @Test
    void getTotalPlayers_shouldDelegateToRepository() {
        when(playerRepository.count()).thenReturn(42L);

        long count = playerService.getTotalPlayers();

        assertEquals(42L, count);
    }
}
