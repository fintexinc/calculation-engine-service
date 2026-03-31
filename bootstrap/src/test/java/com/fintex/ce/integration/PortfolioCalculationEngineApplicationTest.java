package com.fintex.ce.integration;

import com.fintex.ce.PortfolioCalculationEngineApplication;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest(
    classes = PortfolioCalculationEngineApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class PortfolioCalculationEngineApplicationTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void shouldStartApplication_whenApplicationContextIsBuilt() {
    assertThat(applicationContext).isNotNull();
    assertThat(applicationContext.getBeansOfType(SecurityDataFetcher.class)).isNotEmpty();
    assertThat(applicationContext.getBeansWithAnnotation(Service.class)).isNotEmpty();
  }
}
