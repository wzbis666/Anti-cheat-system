package com.anticheat.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "punishments")
public class Punishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(name = "punishment_type", nullable = false)
    private String punishmentType;
    
    @Column(name = "punishment_time", nullable = false)
    private long punishmentTime;
    
    @Column(name = "duration")
    private Long duration;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "unbanned_time")
    private Long unbannedTime;
    
    @Column(name = "unbanned_by")
    private String unbannedBy;

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

    public String getPunishmentType() {
        return punishmentType;
    }

    public void setPunishmentType(String punishmentType) {
        this.punishmentType = punishmentType;
    }

    public long getPunishmentTime() {
        return punishmentTime;
    }

    public void setPunishmentTime(long punishmentTime) {
        this.punishmentTime = punishmentTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getUnbannedTime() {
        return unbannedTime;
    }

    public void setUnbannedTime(Long unbannedTime) {
        this.unbannedTime = unbannedTime;
    }

    public String getUnbannedBy() {
        return unbannedBy;
    }

    public void setUnbannedBy(String unbannedBy) {
        this.unbannedBy = unbannedBy;
    }
}
