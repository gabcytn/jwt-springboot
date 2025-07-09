package com.gabcytyn.jwtdemo.DTO;

public class LoginUserDto {
  // TODO: add validation
  private String email;
  private String password;
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
