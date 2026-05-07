package com.anticheat.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "cheat_records")
public class CheatRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(name = "cheat_type", nullable = false)
    private String cheatType;
    
    @Column(name = "detection_time", nullable = false)
    private long detectionTime;
    
    @Column(name = "severity", nullable = false)
    private int severity;
    
    @Column(name = "details")
    private String details;

    @Column(name = "detection_method")
    private String detectionMethod;

    @Column(name = "evidence", length = 2000)
    private String evidence;

    @Column(name = "server_name")
    private String serverName;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getCheatType() {
        return cheatType;
    }

    public void setCheatType(String cheatType) {
        this.cheatType = cheatType;
    }

    public long getDetectionTime() {
        return detectionTime;
    }

    public void setDetectionTime(long detectionTime) {
        this.detectionTime = detectionTime;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetectionMethod() { return detectionMethod; }
    public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
}
