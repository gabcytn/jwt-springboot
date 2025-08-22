package com.gabcytyn.jwtdemo.Config;

import com.gabcytyn.jwtdemo.DTO.RefreshTokenValidatorDto;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories("com.gabcytyn.jwtdemo.Repository")
public class RedisConfig {
  @Bean
  public LettuceConnectionFactory lettuceConnectionFactory() {
    RedisProperties properties = redisProperties();
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

    configuration.setHostName(properties.getHost());
    configuration.setPort(properties.getPort());

    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public RedisTemplate<byte[], byte[]> redisTemplate() {
    RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory());
    return template;
  }

  @Bean
  public RedisTemplate<String, RefreshTokenValidatorDto> refreshTokenRedisTemplate() {
    RedisTemplate<String, RefreshTokenValidatorDto> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory());
    template.setKeySerializer(StringRedisSerializer.UTF_8);
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }

  @Bean
  @Primary
  public RedisProperties redisProperties() {
    return new RedisProperties();
  }
}
