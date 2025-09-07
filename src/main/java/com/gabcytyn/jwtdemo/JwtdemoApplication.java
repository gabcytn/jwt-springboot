package com.gabcytyn.jwtdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JwtdemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(JwtdemoApplication.class, args);
  }
}
