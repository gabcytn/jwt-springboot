package com.gabcytyn.jwtdemo.Repository;

import com.gabcytyn.jwtdemo.DTO.CacheData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsCacheRepository extends CrudRepository<CacheData, String> {}
