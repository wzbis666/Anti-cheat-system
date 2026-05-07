package com.anticheat.backend.dto;

public class UpdateProfileRequest {

    private String nickname;
    private String email;
    private String avatar;
    private String mcUsername;
    private String mcUuid;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getMcUsername() { return mcUsername; }
    public void setMcUsername(String mcUsername) { this.mcUsername = mcUsername; }
    public String getMcUuid() { return mcUuid; }
    public void setMcUuid(String mcUuid) { this.mcUuid = mcUuid; }
}
