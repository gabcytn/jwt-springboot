package com.gabcytyn.jwtdemo.DTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;

public class RefreshTokenValidatorDto {
  @Id private String key;
  private String email;
  private String deviceName;
  @TimeToLive private Long expiresAt;

  public RefreshTokenValidatorDto(String key, String email, String deviceName) {
    this.key = key;
    this.email = email;
    this.deviceName = deviceName;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public Long getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
  }
}
