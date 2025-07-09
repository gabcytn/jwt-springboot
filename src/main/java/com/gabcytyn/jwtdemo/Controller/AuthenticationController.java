package com.gabcytyn.jwtdemo.Controller;

import com.gabcytyn.jwtdemo.DTO.LoginResponseDto;
import com.gabcytyn.jwtdemo.DTO.LoginUserDto;
import com.gabcytyn.jwtdemo.DTO.RegisterUserDto;
import com.gabcytyn.jwtdemo.DTO.UserPrincipal;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
  private final AuthenticationService authenticationService;

  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody RegisterUserDto user) {
    try {
      authenticationService.signup(user);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      System.err.println("Error signing user up");
      System.err.println(e.getMessage());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(
      @RequestBody LoginUserDto user, HttpServletRequest request, HttpServletResponse response) {
    try {
      LoginResponseDto responseDto = authenticationService.authenticate(request, response, user);
      return new ResponseEntity<>(responseDto, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/refresh-token/{deviceName}")
  public ResponseEntity<LoginResponseDto> refreshToken(
      @PathVariable String deviceName, HttpServletRequest request, HttpServletResponse response) {
    try {
      LoginResponseDto responseDto = authenticationService.newJwt(request, response, deviceName);
      return new ResponseEntity<>(responseDto, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println("Error generating new refresh token");
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // test JWT
  @GetMapping("/users/me")
  public ResponseEntity<Map<String, String>> authenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
    Map<String, String> response = new HashMap<>();
    response.put("email", currentUser.getUsername());
    response.put("password", currentUser.getPassword());
    return ResponseEntity.ok(response);
  }
}
