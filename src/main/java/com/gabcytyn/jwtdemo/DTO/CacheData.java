package com.gabcytyn.jwtdemo.DTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "cacheData")
public class CacheData {
  @Id private String key;
  private String value;
  // defaults to no TTL
  @TimeToLive private Long expiresAt = 0L;

  public CacheData() {}

  public CacheData(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public CacheData(String key, String value, Long expiresAt) {
    this.key = key;
    this.value = value;
    this.expiresAt = expiresAt;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Long getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
  }
}
