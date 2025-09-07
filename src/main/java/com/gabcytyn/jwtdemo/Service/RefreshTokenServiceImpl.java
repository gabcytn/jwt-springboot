package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import com.gabcytyn.jwtdemo.Repository.RefreshTokenRepository;
import com.gabcytyn.jwtdemo.Service.Interface.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public void save(RefreshTokenValidatorDto validator) {
    refreshTokenRepository.save(validator);
  }

  @Override
  public RefreshTokenValidatorDto find(String key) {
    return refreshTokenRepository.findById(key).orElseThrow();
  }

  @Override
  public void delete(String key) {
    refreshTokenRepository.deleteById(key);
  }
}
