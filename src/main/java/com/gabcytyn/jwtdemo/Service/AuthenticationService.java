package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Exception.RefreshTokenException;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
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

  public void signup(RegisterUserDto user) throws AuthenticationException {
    try {
      User userToSave = new User();
      userToSave.setEmail(user.getEmail());
      userToSave.setFullName(user.getFullName());
      userToSave.setPassword(passwordEncoder.encode(user.getPassword()));

      userRepository.save(userToSave);
    } catch (Exception e) {
      System.err.println("Error signing up user: " + user.getEmail());
      System.err.println(e.getMessage());
      throw new AuthenticationException(
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
      RefreshTokenValidatorDto tokenValidatorDto =
          new RefreshTokenValidatorDto(user.getEmail(), user.getDeviceName());
      String generatedRefreshToken = jwtService.generateRefreshToken();
      cachingService.saveRefreshToken(generatedRefreshToken, tokenValidatorDto);
    }
    return new LoginResponseDto(token, jwtService.getExpirationTime());
  }

  public LoginResponseDto newJwt(String refreshToken, String deviceName)
      throws RefreshTokenException {
    try {
      RefreshTokenValidatorDto validator = cachingService.getRefreshTokenValidator(refreshToken);
      if (validator == null)
        throw new RefreshTokenException("Refresh token not found.");
      if (!deviceName.equals(validator.deviceName()))
        throw new RefreshTokenException("Stored device name does not match request's device name");
      String jwt = jwtService.generateToken(validator.email());

      cachingService.deleteRefreshToken(refreshToken);
      String generatedRefreshToken = jwtService.generateRefreshToken();
      cachingService.saveRefreshToken(generatedRefreshToken, validator);
      return new LoginResponseDto(jwt, jwtService.getExpirationTime());
    } catch (Exception e) {
      System.err.println("Error generating new JWT");
      System.err.println(e.getMessage());
      throw new RefreshTokenException(e.getMessage());
    }
  }
}
