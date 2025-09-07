package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import com.gabcytyn.jwtdemo.Service.Interface.RefreshTokenService;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CachingService implements RefreshTokenService {
  private final RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate;

  public CachingService(RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate) {
    this.refreshTokenRedisTemplate = refreshTokenRedisTemplate;
  }

  public void save(RefreshTokenValidatorDto validator) {
    String key = this.getRefreshTokenKey(validator.getKey());
    refreshTokenRedisTemplate.opsForValue().set(key, validator);
    refreshTokenRedisTemplate.expire(key, 7, TimeUnit.DAYS);
  }

  public RefreshTokenValidatorDto find(String refreshToken) {
    return refreshTokenRedisTemplate.opsForValue().get(this.getRefreshTokenKey(refreshToken));
  }

  public void delete(String refreshToken) {
    refreshTokenRedisTemplate.delete(this.getRefreshTokenKey(refreshToken));
  }

  private String getRefreshTokenKey(String refreshToken) {
    return "refresh:" + refreshToken;
  }
}
