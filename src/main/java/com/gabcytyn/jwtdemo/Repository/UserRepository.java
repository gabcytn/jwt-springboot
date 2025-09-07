package com.gabcytyn.jwtdemo.Repository;

import com.gabcytyn.jwtdemo.Entity.User;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
  @Cacheable(value = "user", key = "#email")
  Optional<User> findByEmail(String email);
}
