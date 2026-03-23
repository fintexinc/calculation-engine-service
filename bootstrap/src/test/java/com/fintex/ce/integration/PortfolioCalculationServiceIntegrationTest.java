package com.fintex.ce.integration;

import com.fintex.ce.PortfolioCalculationService;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import org.springframework.stereotype.Service;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest(
    classes = PortfolioCalculationService.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class PortfolioCalculationServiceIntegrationTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void shouldStartApplication_whenApplicationContextIsBuilt() {
    assertThat(applicationContext).isNotNull();
    assertThat(applicationContext.getBeansOfType(SecurityDataFetcher.class)).isNotEmpty();
    assertThat(applicationContext.getBeansWithAnnotation(Service.class)).isNotEmpty();
  }
}
