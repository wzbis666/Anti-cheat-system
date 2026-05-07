package com.anticheat.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "player_sessions")
public class PlayerSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "login_time", nullable = false)
    private long loginTime;

    @Column(name = "logout_time")
    private long logoutTime;

    @Column(name = "duration")
    private long duration;

    @Column(name = "server_name")
    private String serverName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
    public long getLogoutTime() { return logoutTime; }
    public void setLogoutTime(long logoutTime) { this.logoutTime = logoutTime; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
}
