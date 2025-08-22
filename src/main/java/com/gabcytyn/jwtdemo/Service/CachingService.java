package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CachingService {
  private final RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate;

  public CachingService(RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate) {
    this.refreshTokenRedisTemplate = refreshTokenRedisTemplate;
  }

  public void saveRefreshToken(String refreshToken, RefreshTokenValidatorDto validator) {
    refreshTokenRedisTemplate.opsForValue().set(refreshToken, validator);
    refreshTokenRedisTemplate.expire("refresh:" + refreshToken, 7, TimeUnit.DAYS);
  }

  public RefreshTokenValidatorDto getRefreshTokenValidator(String refreshToken) {
    return refreshTokenRedisTemplate.opsForValue().get(refreshToken);
  }

  public void deleteRefreshToken(String refreshToken) {
    refreshTokenRedisTemplate.delete(refreshToken);
  }
}
