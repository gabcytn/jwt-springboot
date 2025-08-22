package com.gabcytyn.jwtdemo.Exception;

public class RefreshTokenException extends RuntimeException {
  public RefreshTokenException(String message) {
    super(message);
  }
}
