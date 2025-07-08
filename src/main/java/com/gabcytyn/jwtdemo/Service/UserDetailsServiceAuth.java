package com.gabcytyn.jwtdemo.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabcytyn.jwtdemo.DTO.CacheData;
import com.gabcytyn.jwtdemo.DTO.UserPrincipal;
import com.gabcytyn.jwtdemo.Entity.User;
import com.gabcytyn.jwtdemo.Repository.UserDetailsCacheRepository;
import com.gabcytyn.jwtdemo.Repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceAuth implements UserDetailsService {
  private final UserRepository userRepository;
  private final UserDetailsCacheRepository userDetailsCacheRepository;
  private final ObjectMapper objectMapper;

  public UserDetailsServiceAuth(
      UserRepository userRepository,
      UserDetailsCacheRepository userDetailsCacheRepository,
      ObjectMapper objectMapper) {
    this.userRepository = userRepository;
    this.userDetailsCacheRepository = userDetailsCacheRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<CacheData> cacheData = userDetailsCacheRepository.findById(email);
    if (cacheData.isPresent()) {
      System.out.println("Cache hit!");
      TypeReference<User> mapType = new TypeReference<>() {};
      try {
        User cachedUser = objectMapper.readValue(cacheData.get().getValue(), mapType);
        return new UserPrincipal(cachedUser);
      } catch (JsonProcessingException e) {
        System.err.println("Error mapping cached string value to user entity");
        throw new UsernameNotFoundException("Error mapping cached string value to user entity");
      }
    }

    System.out.println("Cache miss!");
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      User presentUser = user.get();
      try {
        userDetailsCacheRepository.save(
            new CacheData(
                presentUser.getEmail(), objectMapper.writeValueAsString(presentUser), -1L)); // cache forever
        return new UserPrincipal(presentUser);
      } catch (JsonProcessingException e) {
        System.err.println("Error writing user entity as String");
        throw new UsernameNotFoundException("Error writing user entity as String");
      }
    }

    System.err.println("Username/Email not found.");
    throw new UsernameNotFoundException("Username/Email not found.");
  }
}
