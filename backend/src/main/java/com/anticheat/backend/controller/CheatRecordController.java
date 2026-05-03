package com.anticheat.backend.controller;

import com.anticheat.backend.model.CheatRecord;
import com.anticheat.backend.service.CheatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cheat")
public class CheatRecordController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "cheatType", "severity", "detectionTime", "details"
    );

    private final CheatRecordService cheatRecordService;

    @Autowired
    public CheatRecordController(CheatRecordService cheatRecordService) {
        this.cheatRecordService = cheatRecordService;
    }

    @GetMapping("/all")
    public List<CheatRecord> getAllCheatRecords() {
        return cheatRecordService.getAllCheatRecords();
    }

    @GetMapping("/page")
    public Page<CheatRecord> getCheatRecordsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "detectionTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "detectionTime";
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return cheatRecordService.getCheatRecordsByPage(pageable);
    }

    @GetMapping("/type/{cheatType}")
    public Page<CheatRecord> getCheatRecordsByType(
            @PathVariable String cheatType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("detectionTime").descending());
        return cheatRecordService.getCheatRecordsByType(cheatType, pageable);
    }

    @GetMapping("/player/{uuid}")
    public List<CheatRecord> getCheatRecordsByPlayerUuid(@PathVariable String uuid) {
        return cheatRecordService.getCheatRecordsByPlayerUuid(uuid);
    }

    @PostMapping("/add")
    public ResponseEntity<CheatRecord> addCheatRecord(@RequestBody Map<String, Object> request) {
        String playerName = (String) request.get("playerName");
        String uuid = (String) request.get("uuid");
        String cheatType = (String) request.get("cheatType");
        Integer severity = request.get("severity") != null ? Integer.valueOf(request.get("severity").toString()) : 1;
        String details = (String) request.get("details");

        CheatRecord saved = cheatRecordService.createCheatRecord(playerName, uuid, cheatType, severity, details);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCheatRecord(@PathVariable Long id) {
        cheatRecordService.deleteCheatRecord(id);
        return ResponseEntity.ok(Map.of("message", "作弊记录已删除"));
    }
}
