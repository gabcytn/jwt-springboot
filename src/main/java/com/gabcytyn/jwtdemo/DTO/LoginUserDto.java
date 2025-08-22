package com.gabcytyn.jwtdemo.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginUserDto {
  @NotNull(message = "Email is required.")
  @NotBlank(message = "Email must not be blank.")
  @Email
  private String email;

  @NotNull(message = "Password is required.")
  @NotBlank(message = "Password must not be blank.")
  private String password;

  @NotNull(message = "Device name is required.")
  @NotBlank(message = "Device name must not be blank.")
  private String deviceName;

  public LoginUserDto(String email, String password, String deviceName) {
    this.email = email;
    this.password = password;
    this.deviceName = deviceName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  @Override
  public String toString() {
    return "LoginUserDto{"
        + "email='"
        + email
        + '\''
        + ", password='"
        + password
        + '\''
        + ", deviceName='"
        + deviceName
        + '\''
        + '}';
  }
}
