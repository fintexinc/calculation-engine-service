package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CompositeCalculationRequest;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.observability.CalculationStatisticsReport;
import com.fintex.ce.port.observability.CalculationStatisticsReport.CodeFrequency;
import com.fintex.ce.port.observability.CalculationStatisticsReport.MetricStatistics;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static com.fintex.ce.e2e.PortfolioHoldingBuildHelper.etfCa;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * End-to-end coverage of the business metrics: real traffic over the HTTP boundary, then the statistics read back from
 * {@code /actuator/calculationstats}.
 *
 * <p>
 * One test drives every branch that matters, because the meters accumulate across a request and separate test methods
 * would make each one's expectations depend on execution order. In sequence: a metric that succeeds, the same metric
 * failing, and a composite request failing — which is the claim worth proving end to end, since a composite must
 * contribute one row per member metric and must never appear as a metric called {@code composite}.
 *
 * <p>
 * The response timeout is raised above the {@code WebTestClient} default of five seconds so that a CI agent running
 * several application contexts at once makes this test slow rather than failed.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalculationStatisticsEndpointE2ETest {

  private static final String CALCULATIONS_PATH = "/api/v1/portfolio/calculations";
  private static final String STATISTICS_PATH = "/actuator/calculationstats";
  private static final String ALLOCATIONS = CalculationMetric.ASSET_ALLOCATIONS.getValue();
  private static final String EQUITY_SECTOR = CalculationMetric.EQUITY_SECTOR.getValue();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  private static MockWebServer smsMockServer;

  @Autowired
  private WebTestClient webTestClient;

  @BeforeAll
  static void startSmsMockServer() throws IOException {
    if (smsMockServer == null) {
      smsMockServer = new MockWebServer();
      smsMockServer.start();
    }
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.security-master.rest.base-url", () -> {
      try {
        startSmsMockServer();
        return smsMockServer.url("/").toString().replaceAll("/$", "");
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  @Test
  void shouldReportPerMetricStatistics_whenCalculationsSucceededAndFailedIncludingACompositeRequest() {
    smsMockServer.setDispatcher(alwaysRespond(new MockResponse()
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(writeJson(allocationAttributes()))));
    assertThat(postSingle(ALLOCATIONS, writeJson(allocationsCommand())))
        .isEqualTo(HttpStatus.OK.value());

    smsMockServer.setDispatcher(alwaysRespond(new MockResponse()
        .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody("{\"error\":\"upstream failure\"}")));
    assertThat(postSingle(ALLOCATIONS, writeJson(allocationsCommand())))
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(postComposite(writeJson(compositeRequest())))
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());

    CalculationStatisticsReport report = statistics();

    assertThat(report.metrics()).extracting(MetricStatistics::metric)
        .as("only the metrics that actually ran, most-problematic first")
        .containsExactly(ALLOCATIONS, EQUITY_SECTOR);

    MetricStatistics allocations = metric(report, ALLOCATIONS);
    assertThat(allocations.executions()).isEqualTo(3);
    assertThat(allocations.successes()).isEqualTo(1);
    assertThat(allocations.failures())
        .as("its own failed request plus its membership in the failed composite request")
        .isEqualTo(2);
    assertThat(allocations.failureRatePercent()).isEqualTo(66.67);
    assertThat(allocations.duration().samples())
        .as("only the run that reached the calculation is timed")
        .isEqualTo(1);
    assertThat(allocations.duration().meanMillis()).isNotNegative();
    assertThat(allocations.warnings().total()).isZero();
    assertThat(allocations.topErrorCodes()).extracting(CodeFrequency::code)
        .containsExactly(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode());
    assertThat(allocations.topErrorCodes()).extracting(CodeFrequency::count).containsExactly(2L);

    MetricStatistics equitySector = metric(report, EQUITY_SECTOR);
    assertThat(equitySector.executions())
        .as("a member of the failed composite request, never called on its own")
        .isEqualTo(1);
    assertThat(equitySector.successes()).isZero();
    assertThat(equitySector.failures()).isEqualTo(1);
    assertThat(equitySector.failureRatePercent()).isEqualTo(100.0);
    assertThat(equitySector.duration().samples())
        .as("the fetch failed before any calculation ran, so there is nothing to time")
        .isZero();
    assertThat(equitySector.topErrorCodes()).extracting(CodeFrequency::code)
        .containsExactly(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode());

    assertThat(report.metrics()).extracting(MetricStatistics::metric)
        .as("the endpoint a client happened to call must leave no trace in the numbers")
        .doesNotContain("composite", "unknown");

    assertThat(report.overall().executions()).isEqualTo(4);
    assertThat(report.overall().successes()).isEqualTo(1);
    assertThat(report.overall().failures()).isEqualTo(3);
    assertThat(report.overall().failureRatePercent()).isEqualTo(75.0);
    assertThat(report.overall().topErrorCodes()).extracting(CodeFrequency::code)
        .containsExactly(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE.getCode());
    assertThat(report.overall().topErrorCodes()).extracting(CodeFrequency::count).containsExactly(3L);
  }

  private CalculationStatisticsReport statistics() {
    return webTestClient.get()
        .uri(STATISTICS_PATH)
        .exchange()
        .expectStatus().isOk()
        .expectBody(CalculationStatisticsReport.class)
        .returnResult()
        .getResponseBody();
  }

  private int postSingle(String metricName, String body) {
    return webTestClient.post()
        .uri(CALCULATIONS_PATH + "/" + metricName)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .returnResult(String.class)
        .getStatus()
        .value();
  }

  private int postComposite(String body) {
    return webTestClient.post()
        .uri(CALCULATIONS_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .returnResult(String.class)
        .getStatus()
        .value();
  }

  private static MetricStatistics metric(CalculationStatisticsReport report, String metric) {
    return report.metrics().stream()
        .filter(statistics -> statistics.metric().equals(metric))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no row for metric " + metric));
  }

  private static Dispatcher alwaysRespond(MockResponse response) {
    return new Dispatcher() {

      @Override
      public MockResponse dispatch(RecordedRequest request) {
        return response;
      }
    };
  }

  private static PortfolioHoldingsCommand allocationsCommand() {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.ASSET_ALLOCATIONS)
        .holdings(List.of(etfCa("XBAL", 50_000)))
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .build();
  }

  private static CompositeCalculationRequest compositeRequest() {
    return CompositeCalculationRequest.builder()
        .holdings(List.of(etfCa("XBAL", 50_000)))
        .dataProviders(List.of(DataProvider.MORNINGSTAR))
        .currency(Currency.CAD)
        .commands(List.of(
            PortfolioHoldingsCommand.builder().metric(CalculationMetric.ASSET_ALLOCATIONS).build(),
            PortfolioHoldingsCommand.builder().metric(CalculationMetric.EQUITY_SECTOR).build()))
        .build();
  }

  private static Map<CompositeSecurityAttribute, Object> allocationAttributes() {
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(
        new AssetAllocationValue(AssetAllocationRegionType.US_EQUITIES, BigDecimal.ONE, new TreeSet<>()))));
    allocation.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    CurrencyDatapoint currency = new CurrencyDatapoint();
    currency.setValue(Currency.CAD);

    AssetAllocationWithCurrency withCurrency = new AssetAllocationWithCurrency();
    withCurrency.setAssetAllocation(allocation);
    withCurrency.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    withCurrency.setCurrency(currency);

    return Map.of(CompositeSecurityAttribute.ASSET_ALLOCATION, List.of(
        SecurityAttributeResult.<AssetAllocationWithCurrency>builder()
            .identifier(new SecurityIdentifier("XBAL", FiIdentifierType.TICKER))
            .data(withCurrency)
            .build()));
  }

  private static String writeJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
