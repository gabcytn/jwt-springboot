package com.gabcytyn.jwtdemo.Controller;

import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);
  private final AuthenticationService authenticationService;

  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserDto user)
      throws AuthenticationException {
    authenticationService.signup(user);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(
      @RequestBody @Valid LoginUserDto user,
      @CookieValue(value = "X-REFRESH-TOKEN", required = false) String refreshToken)
      throws Exception {
    LOG.info("Refresh token: {}", refreshToken);
    LoginResponseDto responseDto =
        authenticationService.authenticate(user, Optional.ofNullable(refreshToken));
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponseDto> refreshToken(
      @RequestBody @Valid RefreshTokenRequest tokenRequest,
      @CookieValue(value = "X-REFRESH-TOKEN", required = false) String refreshToken)
      throws Exception {
    if (refreshToken == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    LoginResponseDto responseDto =
        authenticationService.newJwt(refreshToken, tokenRequest.getDeviceName());
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
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
