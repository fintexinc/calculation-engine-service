package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.port.output.cache.CacheCleanupPort;
import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TasksController {

  private final CacheWarmUpPort cacheWarmUpPort;
  private final CacheCleanupPort cacheCleanupPort;

  @PostMapping(value = "garbage-collection")
  public void garbageCollection() {
    System.gc();
  }

  @PostMapping(value = "evict-entire-cache")
  public void evictAll() {
    cacheCleanupPort.clearCache();
    cacheCleanupPort.evictLocalCaches();
  }

  @PostMapping(value = "cache-warm-up")
  public void warmUpCache() {
    cacheWarmUpPort.run();
  }

}