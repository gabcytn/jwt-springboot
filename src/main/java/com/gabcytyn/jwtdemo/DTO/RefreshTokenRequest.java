package com.gabcytyn.jwtdemo.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RefreshTokenRequest {
  @NotNull(message = "Device name is required.")
  @NotBlank(message = "Device name field must not be blank.")
  private String deviceName;

  @JsonCreator
  public RefreshTokenRequest(@JsonProperty("deviceName") String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }
}
