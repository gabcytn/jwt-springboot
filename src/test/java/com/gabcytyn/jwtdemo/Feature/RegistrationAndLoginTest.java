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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationAndLoginTest {
  private final Faker faker = new Faker();
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  public void successfulRegistrationAndLogin() throws Exception {
    String email = getRandomEmail(),
        fullName = getRandomFullName(),
        password = getRandomPassword(),
        deviceName = getRandomWord();
    RegisterUserDto register = new RegisterUserDto(email, fullName, password);
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
        .andDo(print())
        .andExpect(status().isCreated());

    LoginUserDto login = new LoginUserDto(email, password, deviceName);
    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(cookie().exists("X-REFRESH-TOKEN"))
            .andReturn();

    Cookie refreshToken = Objects.requireNonNull(result.getResponse().getCookie("X-REFRESH-TOKEN"));

    RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(deviceName);
    mockMvc
        .perform(
            post("/auth/refresh-token")
                .cookie(refreshToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
        .andDo(print())
        .andExpect(status().isOk());
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
