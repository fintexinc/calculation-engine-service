package com.fintex.ce.port.output.cache;

/**
 * Port interface for cache health checks.
 * Implemented by the cache-adapter module.
 */
public interface CacheHealthPort {

  boolean isHealthy();

}