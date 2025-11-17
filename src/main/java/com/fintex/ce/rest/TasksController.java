package com.fintex.ce.rest;

import com.fintex.ce.service.interfaces.cache.statistic.CacheWarmUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final CacheWarmUpService cacheWarmUpService;
    private final CaffeineCacheManager caffeine1HourCacheManager;

    @PostMapping(value = "garbage-collection")
    public void garbageCollection() {
        System.gc();
    }

    @PostMapping(value = "evict-entire-cache")
    public void evictAll() {
        cacheWarmUpService.clearCache();
        caffeine1HourCacheManager.getCacheNames().forEach(cacheName -> Objects.requireNonNull(caffeine1HourCacheManager.getCache(cacheName)).invalidate());
    }

    @PostMapping(value = "cache-warm-up")
    public void warmUpCache() {
        cacheWarmUpService.run();
    }

}
