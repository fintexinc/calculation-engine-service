package com.fintex.ce.adapter.cache;

import com.fintex.ce.port.output.cache.CacheHealthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthAdapter implements CacheHealthPort {

  public static final String PONG = "pong";
  private final RedisConnectionFactory redisConnectionFactory;

  @Override
  public boolean isHealthy() {
    RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
    try {
      String pong = connection.ping();
      return PONG.equalsIgnoreCase(pong);
    } finally {
      RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
    }
  }

}