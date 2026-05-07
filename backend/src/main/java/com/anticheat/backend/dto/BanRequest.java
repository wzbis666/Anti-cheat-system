package com.anticheat.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BanRequest {

    @NotBlank(message = "玩家名不能为空")
    private String playerName;

    @NotBlank(message = "UUID不能为空")
    private String uuid;

    @NotBlank(message = "处罚类型不能为空")
    private String punishmentType;

    private long duration;

    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getPunishmentType() { return punishmentType; }
    public void setPunishmentType(String punishmentType) { this.punishmentType = punishmentType; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
