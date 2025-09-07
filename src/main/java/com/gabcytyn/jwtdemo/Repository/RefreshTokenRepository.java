package com.gabcytyn.jwtdemo.Repository;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenValidatorDto, String> {}
