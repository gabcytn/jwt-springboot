package com.gabcytyn.jwtdemo.Unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.gabcytyn.jwtdemo.DTO.LoginResponseDto;
import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import com.gabcytyn.jwtdemo.Exception.RefreshTokenException;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import com.gabcytyn.jwtdemo.Service.Interface.RefreshTokenService;
import com.gabcytyn.jwtdemo.Service.JwtService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NewJwtServiceTests {
  private final Faker faker = new Faker();
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private JwtService jwtService;
  @InjectMocks private AuthenticationService authenticationService;
  private String oldRefreshToken;
  private String generatedRefreshToken;
  private String deviceName;
  private String generatedJwt;

  @BeforeEach
  public void init() {
    this.oldRefreshToken = faker.internet().uuid();
    this.generatedRefreshToken = faker.internet().uuid();
    this.generatedJwt = faker.internet().uuid();
    this.deviceName = faker.app().name() + faker.name().name();
  }

  @Test
  public void testRefreshTokenHappyFlow() {
    RefreshTokenValidatorDto validatorDto =
        new RefreshTokenValidatorDto(
            this.generatedRefreshToken, faker.internet().emailAddress(), this.deviceName);

    when(refreshTokenService.find(this.oldRefreshToken)).thenReturn(validatorDto);
    when(jwtService.generateToken(validatorDto.getEmail())).thenReturn(this.generatedJwt);
    when(jwtService.generateRefreshToken()).thenReturn(this.generatedRefreshToken);

    LoginResponseDto response = authenticationService.newJwt(this.oldRefreshToken, this.deviceName);

    assertEquals(this.generatedJwt, response.getToken());

    verify(refreshTokenService, times(1)).delete(this.oldRefreshToken);
    verify(jwtService, times(1)).generateRefreshToken();
    verify(refreshTokenService, times(1)).save(validatorDto);
  }

  @Test
  public void testNullRefreshTokenValidator() {
    when(refreshTokenService.find(anyString())).thenReturn(null);

    assertThrows(
        RefreshTokenException.class,
        () -> authenticationService.newJwt(this.oldRefreshToken, this.deviceName));
  }

  @Test
  public void testMismatchDeviceName() {
    RefreshTokenValidatorDto validatorDto =
        new RefreshTokenValidatorDto(
            this.generatedRefreshToken, faker.internet().emailAddress(), this.deviceName);

    when(refreshTokenService.find(this.oldRefreshToken)).thenReturn(validatorDto);

    assertThrows(
        RefreshTokenException.class,
        () -> authenticationService.newJwt(this.oldRefreshToken, faker.internet().uuid()));
  }
}
