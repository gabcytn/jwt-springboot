package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Service.Interface.RefreshTokenService;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CachingService implements RefreshTokenService {
  private final RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate;
  private final RedisTemplate<String, User> userRedisTemplate;

  public CachingService(
      RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate,
      RedisTemplate<String, User> userRedisTemplate) {
    this.refreshTokenRedisTemplate = refreshTokenRedisTemplate;
    this.userRedisTemplate = userRedisTemplate;
  }

  public void save(String refreshToken, RefreshTokenValidatorDto validator) {
    String key = this.getRefreshTokenKey(refreshToken);
    refreshTokenRedisTemplate.opsForValue().set(key, validator);
    refreshTokenRedisTemplate.expire(key, 7, TimeUnit.DAYS);
  }

  public RefreshTokenValidatorDto find(String refreshToken) {
    return refreshTokenRedisTemplate.opsForValue().get(this.getRefreshTokenKey(refreshToken));
  }

  public void delete(String refreshToken) {
    refreshTokenRedisTemplate.delete(this.getRefreshTokenKey(refreshToken));
  }

  public void saveUser(String username, User value, long ttlInMinutes) {
    String key = this.getUserKey(username);
    userRedisTemplate.opsForValue().set(key, value);
    userRedisTemplate.expire(key, ttlInMinutes, TimeUnit.MINUTES);
  }

  public Optional<User> getUser(String username) {
    return Optional.ofNullable(userRedisTemplate.opsForValue().get(this.getUserKey(username)));
  }

  private String getRefreshTokenKey(String refreshToken) {
    return "refresh:" + refreshToken;
  }

  private String getUserKey(String username) {
    return "user:" + username;
  }
}
