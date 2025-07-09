package com.gabcytyn.jwtdemo.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Repository.UserDetailsCacheRepository;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
  private final UserDetailsCacheRepository userDetailsCacheRepository;
  private final ObjectMapper objectMapper;

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      UserDetailsCacheRepository userDetailsCacheRepository,
      ObjectMapper objectMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.userDetailsCacheRepository = userDetailsCacheRepository;
    this.objectMapper = objectMapper;
  }

  public void signup(RegisterUserDto user) throws Exception {
    try {
      User userToSave = new User();
      userToSave.setEmail(user.getEmail());
      userToSave.setFullName(user.getFullName());
      userToSave.setPassword(passwordEncoder.encode(user.getPassword()));

      userRepository.save(userToSave);
    } catch (Exception e) {
      System.err.println("Error signing up user: " + user.getEmail());
      System.err.println(e.getMessage());
      throw new Exception(
          "User with email of: " + user.getEmail() + " fails to be inserted in the DB.");
    }
  }

  public LoginResponseDto authenticate(LoginUserDto user, HttpServletRequest request)
      throws Exception {
    Authentication authToken =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    Authentication authentication = authenticationManager.authenticate(authToken);

    // generate JWT if user exists
    if (authentication.isAuthenticated()) {
      String token = jwtService.generateToken(user.getEmail());
      Optional<CacheData> cacheData =
          userDetailsCacheRepository.findById(request.getSession().getId() + "-refresh-token");
      if (cacheData.isPresent()) {
        String refreshToken = cacheData.get().getValue();
        Long oneWeekExp = 60L * 60 * 24 * 7;
        RefreshTokenValidatorDto tokenValidatorDto =
            new RefreshTokenValidatorDto(user.getEmail(), user.getDeviceName());
        String tokenValidatorAsString = objectMapper.writeValueAsString(tokenValidatorDto);
        userDetailsCacheRepository.save(
            new CacheData(refreshToken, tokenValidatorAsString, oneWeekExp)); // 1 week
        System.out.println("Refresh token: " + refreshToken);
        // delete old cache
        userDetailsCacheRepository.deleteById(request.getSession().getId() + "-refresh-token");
      } else {
        throw new Exception("No refresh token found");
      }
      return new LoginResponseDto(token, jwtService.getExpirationTime());
    }

    throw new Exception("User not found");
  }

  public LoginResponseDto newJwt(HttpServletRequest request, String deviceName) throws Exception {
    try {
      Cookie refreshTokenCookie = findRefreshTokenCookie(request.getCookies());
      Optional<CacheData> cacheData =
          userDetailsCacheRepository.findById(refreshTokenCookie.getValue());
      if (cacheData.isEmpty()) throw new Exception("Refresh token is invalid.");

      String tokenValidatorAsString = cacheData.get().getValue();
      TypeReference<RefreshTokenValidatorDto> mapType = new TypeReference<>() {};
      RefreshTokenValidatorDto tokenValidatorDto =
          objectMapper.readValue(tokenValidatorAsString, mapType);
      if (!deviceName.equals(tokenValidatorDto.deviceName()))
        throw new Exception("Stored device name does not match request's device name");
      String jwt = jwtService.generateToken(tokenValidatorDto.email());
      return new LoginResponseDto(jwt, jwtService.getExpirationTime());
    } catch (Exception e) {
      System.err.println("Error generating new JWT");
      System.err.println(e.getMessage());
      throw new Exception(e.getMessage());
    }
  }

  private Cookie findRefreshTokenCookie(Cookie[] cookies) throws Exception {
    if (cookies == null) throw new Exception("No cookies found.");

    for (Cookie cookie : cookies) {
      if ("X-REFRESH-TOKEN".equals(cookie.getName())) return cookie;
    }

    throw new Exception("Refresh token cookie not found.");
  }
}
