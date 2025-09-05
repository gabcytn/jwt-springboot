package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Exception.DuplicateEmailException;
import com.gabcytyn.jwtdemo.Exception.RefreshTokenException;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final CachingService cachingService;

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      CachingService cachingService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.cachingService = cachingService;
  }

  public void signup(RegisterUserDto user) {
    try {
      User userToSave = new User();
      userToSave.setEmail(user.getEmail());
      userToSave.setFullName(user.getFullName());
      userToSave.setPassword(passwordEncoder.encode(user.getPassword()));

      userRepository.save(userToSave);
    } catch (DataIntegrityViolationException e) {
      LOG.error("Error signing up user: {}", user.getEmail());
      LOG.error(e.getMessage());
      throw new DuplicateEmailException(
          "User with email of: " + user.getEmail() + " fails to be inserted in the DB.");
    }
  }

  public LoginResponseDto authenticate(LoginUserDto user, Optional<String> refreshToken)
      throws Exception {
    Authentication authToken =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    Authentication authentication = authenticationManager.authenticate(authToken);

    if (!authentication.isAuthenticated()) throw new AuthenticationException("User not found");

    String token = jwtService.generateToken(user.getEmail());
    // for future validation of a refresh token
    if (refreshToken.isEmpty()) {
      String generatedRefreshToken = jwtService.generateRefreshToken();
      RefreshTokenValidatorDto tokenValidatorDto =
          new RefreshTokenValidatorDto(
              generatedRefreshToken, user.getEmail(), user.getDeviceName());
      cachingService.save(generatedRefreshToken, tokenValidatorDto);
    }
    return new LoginResponseDto(token, jwtService.getExpirationTime());
  }

  public LoginResponseDto newJwt(String refreshToken, String deviceName)
      throws RefreshTokenException {
    try {
      RefreshTokenValidatorDto validator = cachingService.find(refreshToken);
      if (validator == null) throw new RefreshTokenException("Refresh token not found.");
      if (!deviceName.equals(validator.getDeviceName()))
        throw new RefreshTokenException("Stored device name does not match request's device name");
      String jwt = jwtService.generateToken(validator.getEmail());

      cachingService.delete(refreshToken);
      String generatedRefreshToken = jwtService.generateRefreshToken();
      cachingService.save(generatedRefreshToken, validator);
      return new LoginResponseDto(jwt, jwtService.getExpirationTime());
    } catch (Exception e) {
      LOG.error("Error generating new JWT");
      LOG.error(e.getMessage());
      throw new RefreshTokenException(e.getMessage());
    }
  }
}
