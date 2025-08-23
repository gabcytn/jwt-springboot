package com.gabcytyn.jwtdemo.Feature;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabcytyn.jwtdemo.DTO.LoginUserDto;
import com.gabcytyn.jwtdemo.DTO.RefreshTokenRequest;
import com.gabcytyn.jwtdemo.DTO.RegisterUserDto;
import com.github.javafaker.Faker;
import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationAndLoginTest {
  private final Faker faker = new Faker();
  private final String cookieName = "X-REFRESH-TOKEN";
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private String email;
  private String fullName;
  private String password;
  private String deviceName;

  @BeforeEach
  public void init() {
    this.email = getRandomEmail();
    this.fullName = getRandomFullName();
    this.password = getRandomPassword();
    this.deviceName = getRandomWord();
  }

  @Test
  public void successfulRegistrationAndLogin() throws Exception {
    RegisterUserDto registerReqBody = new RegisterUserDto(email, fullName, password);
    ResultActions registerActions = this.register(objectMapper.writeValueAsString(registerReqBody));

    registerActions.andExpect(status().isCreated());

    LoginUserDto loginReqBody = new LoginUserDto(email, password, deviceName);
    ResultActions loginActions = this.login(objectMapper.writeValueAsString(loginReqBody));

    MvcResult result =
        loginActions.andExpect(status().isOk()).andExpect(cookie().exists(cookieName)).andReturn();

    Cookie refreshToken = this.getRefreshTokenCookie(result);

    RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(deviceName);
    ResultActions refreshTokenActions =
        this.refreshToken(objectMapper.writeValueAsString(refreshTokenRequest), refreshToken);
    refreshTokenActions.andExpect(status().isOk());
  }

  @Test
  public void invalidDeviceName() throws Exception {
    RegisterUserDto registerReqBody = new RegisterUserDto(email, fullName, password);
    ResultActions registerActions = this.register(objectMapper.writeValueAsString(registerReqBody));

    registerActions.andExpect(status().isCreated());

    LoginUserDto loginReqBody = new LoginUserDto(email, password, deviceName);
    ResultActions loginActions = this.login(objectMapper.writeValueAsString(loginReqBody));

    MvcResult result =
        loginActions.andExpect(status().isOk()).andExpect(cookie().exists(cookieName)).andReturn();

    Cookie refreshToken = this.getRefreshTokenCookie(result);

    RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(this.getRandomWord());
    ResultActions refreshTokenActions =
        this.refreshToken(objectMapper.writeValueAsString(refreshTokenRequest), refreshToken);
    refreshTokenActions.andExpect(status().isBadRequest());
  }

  private ResultActions register(String reqBody) throws Exception {
    return mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(reqBody))
        .andDo(print());
  }

  private ResultActions login(String reqBody) throws Exception {
    return mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(reqBody))
        .andDo(print());
  }

  private ResultActions refreshToken(String reqBody, Cookie cookie) throws Exception {
    return mockMvc
        .perform(
            post("/auth/refresh-token")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
        .andDo(print());
  }

  private Cookie getRefreshTokenCookie(MvcResult result) {
    return Objects.requireNonNull(result.getResponse().getCookie(cookieName));
  }

  private String getRandomEmail() {
    return faker.internet().emailAddress();
  }

  private String getRandomFullName() {
    return faker.name().fullName();
  }

  private String getRandomPassword() {
    return faker.internet().password();
  }

  private String getRandomWord() {
    return faker.app().name() + faker.country().name() + faker.date().toString();
  }
}
