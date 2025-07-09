package com.gabcytyn.jwtdemo.Filter;

import com.gabcytyn.jwtdemo.Service.JwtService;
import com.gabcytyn.jwtdemo.Service.UserDetailsServiceAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    if ("/auth/login".equals(request.getRequestURI()) && !hasRefreshToken(request))
      jwtService.generateRefreshToken(request, response);
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      System.err.println("No auth header / doesn't start with Bearer");
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
    filterChain.doFilter(request, response);
  }

  private boolean hasRefreshToken(HttpServletRequest request) {
    Cookie[] requestCookies = request.getCookies();
    if (requestCookies == null) return false;

    for (Cookie requestCookie : requestCookies)
      if ("X-REFRESH-TOKEN".equals(requestCookie.getName())) return true;

    return false;
  }
}
