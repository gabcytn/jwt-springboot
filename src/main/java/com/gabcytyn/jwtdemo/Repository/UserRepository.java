package com.gabcytyn.jwtdemo.Repository;

import com.gabcytyn.jwtdemo.Entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID>
{
	Optional<User> findByEmail(String email);
}
