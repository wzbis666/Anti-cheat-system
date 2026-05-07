package com.anticheat.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appeals")
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "player_uuid", nullable = false)
    private String playerUuid;

    @Column(name = "punishment_id")
    private Long punishmentId;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "admin_response", length = 2000)
    private String adminResponse;

    @Column(name = "handled_by")
    private String handledBy;

    @Column(name = "create_time", nullable = false)
    private long createTime;

    @Column(name = "handle_time")
    private long handleTime;

    public Appeal() {
        this.createTime = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
    public Long getPunishmentId() { return punishmentId; }
    public void setPunishmentId(Long punishmentId) { this.punishmentId = punishmentId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public long getHandleTime() { return handleTime; }
    public void setHandleTime(long handleTime) { this.handleTime = handleTime; }
}
