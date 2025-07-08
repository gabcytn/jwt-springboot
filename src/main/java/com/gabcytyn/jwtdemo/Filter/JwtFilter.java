package com.gabcytyn.jwtdemo.Filter;

import com.gabcytyn.jwtdemo.Service.JwtService;
import com.gabcytyn.jwtdemo.Service.UserDetailsServiceAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final HandlerExceptionResolver handlerExceptionResolver;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtFilter(
      HandlerExceptionResolver handlerExceptionResolver,
      JwtService jwtService,
      UserDetailsServiceAuth userDetailsServiceAuth) {
    this.handlerExceptionResolver = handlerExceptionResolver;
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsServiceAuth;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      System.err.println(
          "Request does not contain authorization header OR token does not start with Bearer");
      filterChain.doFilter(request, response);
      return;
    }

    final String token = authorizationHeader.substring(7);
    final String userEmail = jwtService.extractUsername(token);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // early return if email is invalid or already authenticated
    if (userEmail == null || authentication != null) {
      System.err.println("user email is null OR authentication is NOT NULL");
      filterChain.doFilter(request, response);
      return;
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

    // early return if token is invalid
    if (!jwtService.isTokenValid(token, userDetails)) {
      System.err.println("Token is invalid");
      filterChain.doFilter(request, response);
      return;
    }

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
    // set refresh token if missing
    if (!hasRefreshToken(request)) {
      Cookie cookie = new Cookie("X-REFRESH-TOKEN", generateRefreshToken());
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge(3600);
      response.addCookie(cookie);
    }
    filterChain.doFilter(request, response);
  }

  private boolean hasRefreshToken(HttpServletRequest request) {
    Cookie[] requestCookies = request.getCookies();
    boolean hasToken = false;
    if (requestCookies != null) {
      for (Cookie requestCookie : requestCookies) {
        String cookieName = requestCookie.getName();
        String cookieValue = requestCookie.getValue();
        System.out.println("Name: " + cookieName);
        System.out.println("Value: " + cookieValue);
        if ("X-REFRESH-TOKEN".equals(cookieName)) hasToken = true;
      }
    }
    return hasToken;
  }

  private String generateRefreshToken() {
    String randomString = generateRandomString();
    return hashString(randomString);
  }

  private String hashString(String text) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] hash = messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException exception) {
      System.err.println("Error generating refresh token");
      System.err.println(exception.getMessage());
      return "";
    }
  }

  private String generateRandomString() {
    byte[] byteArray = new byte[32];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(byteArray);
    StringBuilder sb = new StringBuilder();
    for (byte b : byteArray) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }
}
