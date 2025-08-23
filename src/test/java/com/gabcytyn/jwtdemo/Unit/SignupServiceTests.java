package com.gabcytyn.jwtdemo.Unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.gabcytyn.jwtdemo.DTO.RegisterUserDto;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Exception.DuplicateEmailException;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SignupServiceTests {
  private final Faker faker = new Faker();
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthenticationService authenticationService;
  private String email;
  private String fullName;
  private String password;

  @BeforeEach
  public void init() {
    this.email = getRandomEmail();
    this.fullName = getRandomFullName();
    this.password = getRandomPassword();
  }

  @Test
  public void testSignupHappyFlow() {
    RegisterUserDto userDto = new RegisterUserDto(email, fullName, password);
    authenticationService.signup(userDto);

    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  public void testSignupDuplicateEmail() {
    RegisterUserDto userDto = new RegisterUserDto(email, fullName, password);

    // thrown when there's duplicate email
    given(userRepository.save(any(User.class)))
        .willThrow(org.springframework.dao.DataIntegrityViolationException.class);

    assertThrows(DuplicateEmailException.class, () -> authenticationService.signup(userDto));

    verify(userRepository, times(1)).save(any(User.class));
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
}
