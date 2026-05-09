package com.anticheat.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "player_name", unique = true, nullable = false)
    private String playerName;
    
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;
    
    @Column(name = "risk_score", nullable = false)
    private int riskScore = 0;
    
    @Column(name = "last_seen")
    private Long lastSeen;

    @Column(name = "first_seen")
    private Long firstSeen;

    @Column(name = "last_ip")
    private String lastIp;

    @Column(name = "kick_count", nullable = false)
    private int kickCount = 0;
    
    @OneToMany(mappedBy = "player", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JsonIgnore
    private List<CheatRecord> cheatRecords;
    
    @OneToMany(mappedBy = "player", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JsonIgnore
    private List<Punishment> punishments;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Long getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Long firstSeen) { this.firstSeen = firstSeen; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    public int getKickCount() {
        return kickCount;
    }

    public void setKickCount(int kickCount) {
        this.kickCount = kickCount;
    }

    public void incrementKickCount() {
        this.kickCount++;
    }

    public List<CheatRecord> getCheatRecords() {
        return cheatRecords;
    }

    public void setCheatRecords(List<CheatRecord> cheatRecords) {
        this.cheatRecords = cheatRecords;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    public void setPunishments(List<Punishment> punishments) {
        this.punishments = punishments;
    }
}
