package com.fintex.ce.rest;

import com.fintex.ce.adapter.rest.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

class HealthControllerTest {

  private static final String HEALTHY_RESPONSE = "healthy";

  @Test
  void getLiveness_checkResult() {
    final var sut = new HealthController();
    final var response = sut.getLiveness();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(HEALTHY_RESPONSE, response.getBody());
  }

  @Test
  void getReadiness_checkResult() {
    final var sut = new HealthController();
    final var response = sut.getReadiness();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(HEALTHY_RESPONSE, response.getBody());
  }

  @Test
  void getF5HealthCheck_checkResult() {
    final var sut = new HealthController();
    final var response = sut.getF5HealthCheck();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(HEALTHY_RESPONSE, response.getBody());
  }

}
