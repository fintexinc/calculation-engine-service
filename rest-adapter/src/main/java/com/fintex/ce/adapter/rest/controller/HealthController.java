package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.port.output.cache.CacheWarmUpPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final String HEALTHY_RESPONSE = "healthy";
  private static final String ERROR_RESPONSE = "error";
  private final CacheWarmUpPort cacheWarmUpPort;

  @Autowired
  public HealthController(final CacheWarmUpPort cacheWarmUpPort) {
    this.cacheWarmUpPort = cacheWarmUpPort;
  }

  @GetMapping(value = "/liveness")
  public ResponseEntity<String> getLiveness() {
    return new ResponseEntity<>(HEALTHY_RESPONSE, HttpStatus.OK);
  }

  @GetMapping(value = "/readiness")
  public ResponseEntity<String> getReadiness() {
    return new ResponseEntity<>(HEALTHY_RESPONSE, HttpStatus.OK);
  }

  @GetMapping(value = "/health.html")
  public ResponseEntity<String> getF5HealthCheck() {
    return new ResponseEntity<>(HEALTHY_RESPONSE, HttpStatus.OK);
  }

  @GetMapping(value = "/last-time-cache-warm-up-had-run")
  public ResponseEntity<String> cacheWarmupHealthCheck() {
    try {
      final CacheWarmUpPort.SchedulerRunInfo schedulerRunInfo = cacheWarmUpPort
          .cacheWarmUpSchedulerRunCheck();
      if (!schedulerRunInfo.runInLast24Hours()) {
        return new ResponseEntity<>(ERROR_RESPONSE + ", cache warm-up scheduler didn't run in past 24 hours",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return new ResponseEntity<>(HEALTHY_RESPONSE + ", last time cache warm-up scheduler run: " + schedulerRunInfo
          .lastTimeRun(), HttpStatus.OK);
    } catch (final Exception e) {
      return new ResponseEntity<>(ERROR_RESPONSE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
