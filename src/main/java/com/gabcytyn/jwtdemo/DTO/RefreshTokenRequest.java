package com.gabcytyn.jwtdemo.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenRequest {
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
