package com.fintex.ce.adapter.rest.controller.actuator;

import com.fintex.ce.adapter.rest.controller.actuator.FdsHealthIndicator;
import com.fintex.smclient.config.properties.GraphqlTransportProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FdsHealthIndicatorTest {

  @Test
  void getHealthBuilder_checkResult_whenStatusIsOk() {
    // SETUP
    final var sut = mock(FdsHealthIndicator.class);

    doCallRealMethod().when(sut).getHealthBuilder(any());

    // ACT
    final Health.Builder actual = sut.getHealthBuilder(HttpStatus.OK);

    // VERIFY
    final Health build = actual.build();
    assertEquals(Status.UP, build.getStatus());
  }

  @Test
  void getHealthBuilder_checkResult_whenStatusIsNotFound() {
    // SETUP
    final var sut = mock(FdsHealthIndicator.class);

    doCallRealMethod().when(sut).getHealthBuilder(any());

    // ACT
    final Health.Builder actual = sut.getHealthBuilder(HttpStatus.NOT_FOUND);

    // VERIFY
    final Health build = actual.build();
    assertEquals(Status.DOWN, build.getStatus());
  }

  @Test
  void getFdsBaseUrl_checkResult() {
    // SETUP
    final var graphqlProperties = mock(GraphqlTransportProperties.class);
    final var sut = mock(FdsHealthIndicator.class, withSettings().useConstructor(null, graphqlProperties));
    final var excepted = "http://localhost";

    when(graphqlProperties.getLocation()).thenReturn(excepted + "/actuator/health");
    doCallRealMethod().when(sut).getFdsBaseUrl();

    // ACT
    final String actual = sut.getFdsBaseUrl();

    // VERIFY
    assertEquals(excepted, actual);
  }

}