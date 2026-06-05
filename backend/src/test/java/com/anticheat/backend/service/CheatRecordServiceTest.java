package com.anticheat.backend.service;

import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.model.Player;
import com.anticheat.backend.repository.CheatRecordRepository;
import com.anticheat.backend.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheatRecordServiceTest {

    @Mock
    private CheatRecordRepository cheatRecordRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PunishmentService punishmentService;

    @Mock
    private SystemSettingsService settingsService;

    @InjectMocks
    private CheatRecordService cheatRecordService;

    private Player player;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cheatRecordService, "playerRepository", playerRepository);
        ReflectionTestUtils.setField(cheatRecordService, "punishmentService", punishmentService);
        ReflectionTestUtils.setField(cheatRecordService, "settingsService", settingsService);

        player = new Player();
        player.setId(1L);
        player.setPlayerName("TestPlayer");
        player.setUuid("test-uuid-123");
        player.setRiskScore(5);
    }

    @Test
    void createCheatRecord_shouldSaveRecordAndUpdateRiskScore() {
        when(playerRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(player));
        when(cheatRecordRepository.save(any(CheatRecord.class))).thenAnswer(inv -> {
            CheatRecord r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(settingsService.getBooleanSetting("progressive_punishment_enabled", true)).thenReturn(false);

        CheatRecord result = cheatRecordService.createCheatRecord(
                "TestPlayer", "test-uuid-123", "FLY", 3, "Suspicious flight detected");

        assertNotNull(result);
        assertEquals("FLY", result.getCheatType());
        assertEquals(3, result.getSeverity());
        assertEquals(8, player.getRiskScore());
        verify(cheatRecordRepository).save(any(CheatRecord.class));
    }

    @Test
    void createCheatRecord_shouldCreatePlayerIfNotExists() {
        when(playerRepository.findByUuid("new-uuid")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> {
            Player p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(cheatRecordRepository.save(any(CheatRecord.class))).thenAnswer(inv -> {
            CheatRecord r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(settingsService.getBooleanSetting("progressive_punishment_enabled", true)).thenReturn(false);

        CheatRecord result = cheatRecordService.createCheatRecord(
                "NewPlayer", "new-uuid", "SPEED", 2, "Fast movement");

        assertNotNull(result);
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void deleteCheatRecord_shouldDeleteById() {
        cheatRecordService.deleteCheatRecord(1L);

        verify(cheatRecordRepository).deleteById(1L);
    }

    @Test
    void getAllCheatRecords_shouldReturnAll() {
        CheatRecord record = new CheatRecord();
        record.setId(1L);
        record.setCheatType("FLY");
        when(cheatRecordRepository.findAll()).thenReturn(List.of(record));

        List<CheatRecord> results = cheatRecordService.getAllCheatRecords();

        assertEquals(1, results.size());
        assertEquals("FLY", results.get(0).getCheatType());
    }

    @Test
    void getCheatRecordsByPlayerUuid_shouldReturnPlayerRecords() {
        CheatRecord record = new CheatRecord();
        record.setId(1L);
        record.setPlayer(player);
        when(cheatRecordRepository.findByPlayerUuid("test-uuid-123")).thenReturn(List.of(record));

        List<CheatRecord> results = cheatRecordService.getCheatRecordsByPlayerUuid("test-uuid-123");

        assertEquals(1, results.size());
    }

    @Test
    void getTotalCheats_shouldReturnCount() {
        when(cheatRecordRepository.count()).thenReturn(100L);

        long count = cheatRecordService.getTotalCheats();

        assertEquals(100L, count);
    }

    @Test
    void getCheatTypeStatistics_shouldGroupByType() {
        Object[] row1 = {"FLY", 30};
        Object[] row2 = {"SPEED", 20};
        when(cheatRecordRepository.countByCheatType()).thenReturn(List.of(row1, row2));

        Map<String, Integer> stats = cheatRecordService.getCheatTypeStatistics();

        assertEquals(2, stats.size());
        assertEquals(30, stats.get("FLY"));
        assertEquals(20, stats.get("SPEED"));
    }
}
