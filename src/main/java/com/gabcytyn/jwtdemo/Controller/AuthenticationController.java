package com.gabcytyn.jwtdemo.Controller;

import com.gabcytyn.jwtdemo.DTO.*;
import com.gabcytyn.jwtdemo.Exception.AuthenticationException;
import com.gabcytyn.jwtdemo.Service.AuthenticationService;
import jakarta.validation.Valid;
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
  public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserDto user)
      throws AuthenticationException {
    authenticationService.signup(user);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginUserDto user)
      throws Exception {
    LoginResponseDto responseDto = authenticationService.authenticate(user);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponseDto> refreshToken(
      @RequestBody @Valid RefreshTokenRequest tokenRequest,
      @CookieValue("X-REFRESH-TOKEN") String refreshToken)
      throws Exception {
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
