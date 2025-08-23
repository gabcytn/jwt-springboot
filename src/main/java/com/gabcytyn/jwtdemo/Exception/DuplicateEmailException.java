package com.gabcytyn.jwtdemo.Exception;

public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException() {}

  public DuplicateEmailException(String message) {
    super(message);
  }
}
