package com.fintex.ce;

import com.fintex.ce.port.observability.CalculationObservability;
import com.fintex.ce.port.observability.CalculationStatisticsProvider;
import com.fintex.ce.port.webclient.mic.SecurityAttributesFetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PortfolioCalculationEngineApplicationTest {

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private MeterRegistry meterRegistry;
  @Autowired
  private ObservationRegistry observationRegistry;
  @Autowired
  private Tracer tracer;
  @Autowired
  private CalculationObservability calculationObservability;
  @Autowired
  private CalculationStatisticsProvider calculationStatisticsProvider;

  @Test
  void shouldStartApplication_whenApplicationContextIsBuilt() {
    assertThat(applicationContext).isNotNull();
    assertThat(applicationContext.getBeansOfType(SecurityAttributesFetcher.class)).isNotEmpty();
    assertThat(applicationContext.getBeansWithAnnotation(Service.class)).isNotEmpty();
    assertThat(meterRegistry).isNotNull();
    assertThat(observationRegistry).isNotNull();
    assertThat(tracer).isNotNull();
    assertThat(calculationObservability).isNotNull();
    assertThat(calculationStatisticsProvider).isNotNull();
  }
}
