package com.fintex.ce.adapter.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final String HEALTHY_RESPONSE = "healthy";

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

}
