package com.gabcytyn.jwtdemo.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterUserDto {
  @NotNull(message = "Email is required.")
  @NotBlank(message = "Email must not be blank.")
  @Email
  private String email;

  @NotNull(message = "Fullname is required.")
  @NotBlank(message = "Fullname must not be blank.")
  private String fullName;

  @NotNull(message = "Password is required.")
  @NotBlank(message = "Password must not be blank.")
  private String password;

  public RegisterUserDto(String email, String fullName, String password) {
    this.email = email;
    this.fullName = fullName;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "RegisterUserDto{"
        + "email='"
        + email
        + '\''
        + ", fullName='"
        + fullName
        + '\''
        + ", password='"
        + password
        + '\''
        + '}';
  }
}
