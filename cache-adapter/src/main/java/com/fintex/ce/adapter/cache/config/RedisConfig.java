package com.fintex.ce.adapter.cache.config;

import com.fintex.ce.adapter.cache.config.properties.RedisProperties;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

  @Value("${spring.lettuce.pool.max-idle}")
  private Integer maxIdle;
  @Value("${spring.lettuce.pool.min-idle}")
  private Integer minIdle;
  @Value("${spring.lettuce.pool.max-active}")
  private Integer maxActive;

  private final RedisProperties redisProperties;

  public static String PREFIX_ENV;

  @Value("${spring.redis.key-prefix}")
  public void setPrefixEnv(String prefix) {
    RedisConfig.PREFIX_ENV = prefix;
    // Also set in the base RedisId class for new entity instances
    RedisId.DEFAULT_PREFIX_ENV = prefix;
  }

  public RedisConfig(RedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(redisProperties.getHost());
    redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
    redisStandaloneConfiguration.setPort(redisProperties.getPort());
    if (redisProperties.getPassword() != null) {
      redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
    }

    final LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
        .commandTimeout(Duration.ofMillis(redisProperties.getTimeout()))
        .poolConfig(getLettucePoolConfig())
        .useSsl()
        .build();

    return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
    final RedisTemplate<String, Object> template = new RedisTemplate<>();
    final RedisSerializer<Object> valuesSerializer = new GenericJackson2JsonRedisSerializer();
    final RedisSerializer<String> keysSerializer = new StringRedisSerializer();
    template.setConnectionFactory(redisConnectionFactory);
    template.setValueSerializer(valuesSerializer);
    template.setKeySerializer(keysSerializer);
    return template;
  }

  private GenericObjectPoolConfig<Object> getLettucePoolConfig() {
    GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
    genericObjectPoolConfig.setMaxIdle(maxIdle);
    genericObjectPoolConfig.setMinIdle(minIdle);
    genericObjectPoolConfig.setMaxTotal(maxActive);
    genericObjectPoolConfig.setMaxWait(Duration.ofMillis(redisProperties.getTimeout()));
    genericObjectPoolConfig.setTestOnBorrow(true);
    genericObjectPoolConfig.setTestWhileIdle(true);
    return genericObjectPoolConfig;
  }

}