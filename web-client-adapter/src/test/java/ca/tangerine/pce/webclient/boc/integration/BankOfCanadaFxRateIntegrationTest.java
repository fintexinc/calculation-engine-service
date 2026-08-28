package ca.tangerine.pce.webclient.boc.integration;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.port.observability.ExternalCallObservability;
import ca.tangerine.pce.port.webclient.boc.FxRatesFetcher;
import ca.tangerine.pce.webclient.boc.client.BankOfCanadaWebClientConfig;
import ca.tangerine.pce.webclient.resilience.ExternalCallResilienceConfig;
import ca.tangerine.wm.commons.domain.currency.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@Tag("integration")
@SpringBootTest(classes = BankOfCanadaFxRateIntegrationTest.TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "external-services.bank-of-canada.currency-pairs.USD_CAD.rate-sources[0].path=/observations/FXUSDCAD/json",
    "external-services.bank-of-canada.currency-pairs.USD_CAD.rate-sources[0].series-names[0]=FXUSDCAD",
    "external-services.bank-of-canada.currency-pairs.USD_CAD.rate-sources[0].frequency=DAILY",
    "external-services.bank-of-canada.timeout=60000"
})
class BankOfCanadaFxRateIntegrationTest {

  private static MockWebServer bankOfCanadaMockServer;

  @Autowired
  private FxRatesFetcher fxRatesFetcher;

  @Autowired
  private ObjectMapper objectMapper;

  private static void ensureBankOfCanadaMockServerStarted() throws IOException {
    if (bankOfCanadaMockServer == null) {
      bankOfCanadaMockServer = new MockWebServer();
      bankOfCanadaMockServer.start();
    }
  }

  @BeforeAll
  static void startMockServer() throws IOException {
    ensureBankOfCanadaMockServerStarted();
  }

  @AfterAll
  static void stopMockServer() throws IOException {
    if (bankOfCanadaMockServer != null) {
      bankOfCanadaMockServer.shutdown();
      bankOfCanadaMockServer = null;
    }
  }

  @DynamicPropertySource
  static void registerBankOfCanadaBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.bank-of-canada.base-url",
        BankOfCanadaFxRateIntegrationTest::bankOfCanadaMockBaseUrl);
  }

  private static String bankOfCanadaMockBaseUrl() {
    try {
      ensureBankOfCanadaMockServerStarted();
      return bankOfCanadaMockServer.url("/").toString().replaceAll("/$", "");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void shouldOmitFxRate_whenRequiredDateIsUnavailable() throws Exception {
    bankOfCanadaMockServer.enqueue(new MockResponse()
        .setBody(objectMapper.writeValueAsString(Map.of(
            "observations", List.of(
                Map.of("d", "2024-01-31", "FXUSDCAD", Map.of("v", "1.3450")),
                Map.of("d", "2024-02-29")))))
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    NavigableMap<LocalDate, BigDecimal> rates = fxRatesFetcher.fetch(
        new CurrencyExchangePair(Currency.USD, Currency.CAD),
        new DateRange(LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29)));

    RecordedRequest request = bankOfCanadaMockServer.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath())
        .isEqualTo("/observations/FXUSDCAD/json?start_date=2024-01-31&end_date=2024-02-29");
    assertThat(rates).containsExactly(
        Map.entry(LocalDate.of(2024, 1, 31), new BigDecimal("1.3450")));
    assertThat(rates).doesNotContainKey(LocalDate.of(2024, 2, 29));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({BankOfCanadaWebClientConfig.class, ExternalCallResilienceConfig.class})
  @ComponentScan(basePackages = {
      "ca.tangerine.pce.webclient.boc",
      "ca.tangerine.pce.webclient.observability"
  })
  static class TestConfiguration {

    @Bean
    ExternalCallObservability externalCallObservability() {
      return ExternalCallObservability.NO_OP;
    }
  }
}
