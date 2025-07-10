package com.gabcytyn.jwtdemo.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Exception.RefreshTokenException;
import com.gabcytyn.jwtdemo.Repository.RedisCacheRepository;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  private final RedisCacheRepository redisCacheRepository;
  private final ObjectMapper objectMapper;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final Long oneWeek = 60L * 60 * 24 * 7;

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      RedisCacheRepository redisCacheRepository,
      ObjectMapper objectMapper, HttpServletRequest request,
      HttpServletResponse response) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.redisCacheRepository = redisCacheRepository;
    this.objectMapper = objectMapper;
    this.request = request;
    this.response = response;
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

  public LoginResponseDto authenticate(LoginUserDto user)
      throws Exception {
    Authentication authToken =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    Authentication authentication = authenticationManager.authenticate(authToken);

    if (!authentication.isAuthenticated()) throw new AuthenticationException("User not found");

    String token = jwtService.generateToken(user.getEmail());
    // for future validation of a refresh token
    RefreshTokenValidatorDto tokenValidatorDto =
        new RefreshTokenValidatorDto(user.getEmail(), user.getDeviceName());
    String tokenValidatorAsString = objectMapper.writeValueAsString(tokenValidatorDto);
    if (!hasRefreshToken(request))
      jwtService.generateRefreshToken(response, tokenValidatorAsString, oneWeek);
    return new LoginResponseDto(token, jwtService.getExpirationTime());
  }

  public LoginResponseDto newJwt(String deviceName)
      throws RefreshTokenException {
    try {
      Cookie refreshTokenCookie = findRefreshTokenCookie(request.getCookies());
      Optional<CacheData> cacheData = redisCacheRepository.findById(refreshTokenCookie.getValue());
      if (cacheData.isEmpty()) throw new RefreshTokenException("Refresh token is invalid.");

      String tokenValidatorAsString = cacheData.get().getValue();
      TypeReference<RefreshTokenValidatorDto> mapType = new TypeReference<>() {};
      RefreshTokenValidatorDto tokenValidatorDto =
          objectMapper.readValue(tokenValidatorAsString, mapType);
      if (!deviceName.equals(tokenValidatorDto.deviceName()))
        throw new RefreshTokenException("Stored device name does not match request's device name");
      String jwt = jwtService.generateToken(tokenValidatorDto.email());

      // delete old refresh token
      redisCacheRepository.delete(cacheData.get());
      jwtService.generateRefreshToken(response, tokenValidatorAsString, oneWeek);
      return new LoginResponseDto(jwt, jwtService.getExpirationTime());
    } catch (Exception e) {
      System.err.println("Error generating new JWT");
      System.err.println(e.getMessage());
      throw new RefreshTokenException(e.getMessage());
    }
  }

  private Cookie findRefreshTokenCookie(Cookie[] cookies) throws Exception {
    if (cookies == null) throw new RefreshTokenException("No cookies found.");

    for (Cookie cookie : cookies) {
      if ("X-REFRESH-TOKEN".equals(cookie.getName())) return cookie;
    }

    throw new RefreshTokenException("Refresh token cookie not found.");
  }

  private boolean hasRefreshToken(HttpServletRequest request) {
    Cookie[] requestCookies = request.getCookies();
    if (requestCookies == null) return false;

    for (Cookie requestCookie : requestCookies)
      if ("X-REFRESH-TOKEN".equals(requestCookie.getName())) return true;

    return false;
  }
}
