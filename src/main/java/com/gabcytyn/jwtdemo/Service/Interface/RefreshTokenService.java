package com.gabcytyn.jwtdemo.Service.Interface;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;

public interface RefreshTokenService {
	void save(RefreshTokenValidatorDto validator);
	RefreshTokenValidatorDto find(String key);
	void delete(String key);
}
