package com.gabcytyn.jwtdemo.Unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.gabcytyn.jwtdemo.DTO.LoginResponseDto;
import com.gabcytyn.jwtdemo.DTO.LoginUserDto;
import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import com.gabcytyn.jwtdemo.Service.CachingService;
import com.gabcytyn.jwtdemo.Service.JwtService;
import com.github.javafaker.Faker;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTests {
  private final Faker faker = new Faker();
  @Mock private UserRepository userRepository;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private Authentication authenticationMock;
  @Mock private CachingService cachingService;

  @InjectMocks private AuthenticationService authenticationService;
  private String email;
  private String password;
  private String deviceName;

  @BeforeEach
  public void init() {
    this.email = faker.name().fullName();
    this.password = faker.internet().password();
    this.deviceName = faker.app().name() + faker.country().name();
  }

  @Test
  public void testLoginHappyFlowWithoutRefreshTokenCookie() throws Exception {
    LoginUserDto loginDto = new LoginUserDto(this.email, this.password, this.deviceName);
    String generatedToken = faker.internet().uuid();

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(this.authenticationMock);
    when(this.authenticationMock.isAuthenticated()).thenReturn(true);
    when(jwtService.generateToken(anyString())).thenReturn(generatedToken);
    when(jwtService.generateRefreshToken()).thenReturn(anyString());

    LoginResponseDto responseDto = authenticationService.authenticate(loginDto, Optional.empty());

    assertNotNull(responseDto);
    assertEquals(generatedToken, responseDto.getToken());
    verify(authenticationManager).authenticate(any(Authentication.class));
    verify(jwtService, times(1)).generateToken(loginDto.getEmail());
    verify(jwtService, times(1)).getExpirationTime();
    verify(cachingService, times(1))
        .saveRefreshToken(anyString(), any(RefreshTokenValidatorDto.class));
  }

  @Test
  public void testLoginHappyFlowWithRefreshTokenCookie() throws Exception {
    LoginUserDto loginDto = new LoginUserDto(this.email, this.password, this.deviceName);
    String generatedToken = faker.internet().uuid();

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(this.authenticationMock);
    when(this.authenticationMock.isAuthenticated()).thenReturn(true);
    when(jwtService.generateToken(anyString())).thenReturn(generatedToken);

    LoginResponseDto responseDto =
        authenticationService.authenticate(loginDto, Optional.of(anyString()));

    assertNotNull(responseDto);
    assertEquals(generatedToken, responseDto.getToken());
    verify(authenticationManager).authenticate(any(Authentication.class));
    verify(jwtService, times(1)).generateToken(loginDto.getEmail());
    verify(jwtService, times(1)).getExpirationTime();
    verify(jwtService, never()).generateRefreshToken();
    verify(cachingService, never())
        .saveRefreshToken(anyString(), any(RefreshTokenValidatorDto.class));
  }

  @Test
  public void testLoginInvalidCredentials() {
    LoginUserDto loginDto = new LoginUserDto(this.email, this.password, this.deviceName);

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(this.authenticationMock);
    when(this.authenticationMock.isAuthenticated()).thenReturn(false);

    assertThrows(
        AuthenticationException.class,
        () -> authenticationService.authenticate(loginDto, Optional.empty()));
  }
}
