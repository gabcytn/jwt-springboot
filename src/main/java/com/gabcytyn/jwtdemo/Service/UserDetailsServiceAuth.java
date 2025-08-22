package com.gabcytyn.jwtdemo.Service;

import com.gabcytyn.jwtdemo.DTO.UserPrincipal;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceAuth implements UserDetailsService {
  private final CachingService cachingService;
  private final UserRepository userRepository;

  public UserDetailsServiceAuth(CachingService cachingService, UserRepository userRepository) {
    this.cachingService = cachingService;
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<User> cachedUser = cachingService.getUser(email);
    if (cachedUser.isPresent()) {
      System.out.println("Cache hit!");
      return new UserPrincipal(cachedUser.get());
    }

    System.out.println("Cache miss!");
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      User presentUser = user.get();
      cachingService.saveUser(email, presentUser, 15);
      return new UserPrincipal(presentUser);
    }

    System.err.println("Username/Email not found.");
    throw new UsernameNotFoundException("Username/Email not found.");
  }
}
