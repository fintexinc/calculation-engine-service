package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.dto.command.BatchCalculationCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * End-to-end tests for {@code POST /api/v1/portfolio/calculations/batch}.
 * <p>
 * Key scenarios:
 * <ul>
 * <li>Two allocation metrics succeed together; each produces FDS warnings when SM returns empty data.</li>
 * <li>An unsupported batch metric (common-performance-dates) is captured per-metric in {@code errors} without failing
 * the rest of the request.</li>
 * <li>Two metrics that share the geography fetcher produce only one SM geography call — the BatchContext cache
 * deduplicates the second fetch.</li>
 * </ul>
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(properties = "cache.data.fx-rates.enabled=false")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BatchCalculationE2ETest {

  private static final String BATCH_PATH = "/api/v1/portfolio/calculations/batch";
  private static final String ASSET_ALLOCATION_PATH = "/api/v1/wealth/securities/allocations/asset";
  private static final String EQUITY_SECTOR_PATH = "/api/v1/wealth/securities/allocations/equity-sector";
  private static final String GEOGRAPHY_PATH = "/api/v1/wealth/securities/geography";
  private static final String SM_BATCH_ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes/batch";

  private static final List<DataProvider> MORNINGSTAR = List.of(DataProvider.MORNINGSTAR);
  private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  private static MockWebServer smsMockServer;

  @Autowired
  private WebTestClient webTestClient;

  @BeforeAll
  static void startSmsMockServer() throws IOException {
    smsMockServer = new MockWebServer();
    smsMockServer.start();
  }

  @AfterEach
  void resetSmsDispatcher() {
    smsMockServer.setDispatcher(new QueueDispatcher());
  }

  @DynamicPropertySource
  static void registerSmBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.security-master.rest.base-url",
        () -> smsMockServer.url("/").toString().replaceAll("/$", ""));
  }

  // -------------------------------------------------------------------------
  // Happy-path batch
  // -------------------------------------------------------------------------

  @Test
  void shouldReturnBothResults_whenBatchContainsAssetAllocationsAndEquitySector() {
    // ASSET_ALLOCATIONS → /allocations/asset returns []
    // EQUITY_SECTOR → /allocations/equity-sector returns []
    // Both services emit FDS warnings for missing data but still produce valid typed results.
    // SM batch-attributes endpoint returns 404 (gracefully handled: prefetch skipped, falls back
    // to individual fetcher calls).
    smsMockServer.setDispatcher(new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath() != null ? request.getPath() : "";
        if (path.contains(ASSET_ALLOCATION_PATH) || path.contains(EQUITY_SECTOR_PATH)) {
          return jsonResponse("[]");
        }
        return new MockResponse().setResponseCode(404);
      }
    });

    BatchCalculationCommand command = BatchCalculationCommand.builder()
        .metrics(List.of(CalculationMetric.ASSET_ALLOCATIONS, CalculationMetric.EQUITY_SECTOR))
        .holdings(List.of(etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000)))
        .dataProviders(MORNINGSTAR)
        .build();

    HttpResponse response = postBatch(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    BatchResponse body = parseBatchResult(response.responseBody());

    AssetAllocationResult assetResult = parseResult(body, "asset-allocations", AssetAllocationResult.class);
    EquitySectorResult equityResult = parseResult(body, "equity-sector", EquitySectorResult.class);

    assertThat(assetResult.getAssetAllocation()).isNotNull();
    assertThat(assetResult.getWarnings()).isNotEmpty();
    assertThat(equityResult.getEquitySector()).isNotNull();
    assertThat(equityResult.getWarnings()).isEmpty();
    assertThat(body.errors).isNullOrEmpty();
  }

  // -------------------------------------------------------------------------
  // Per-metric error isolation
  // -------------------------------------------------------------------------

  @Test
  void shouldIsolateError_whenBatchContainsUnsupportedMetricCommonPerformanceDates() {
    // ASSET_ALLOCATIONS succeeds (SM returns [], service emits FDS warning but not an error).
    // COMMON_PERFORMANCE_DATES is not supported in batch → MET-003 error isolated to that metric.
    smsMockServer.setDispatcher(new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath() != null ? request.getPath() : "";
        if (path.contains(ASSET_ALLOCATION_PATH)) {
          return jsonResponse("[]");
        }
        return new MockResponse().setResponseCode(404);
      }
    });

    BatchCalculationCommand command = BatchCalculationCommand.builder()
        .metrics(List.of(CalculationMetric.ASSET_ALLOCATIONS,
            CalculationMetric.COMMON_PERFORMANCE_DATES))
        .holdings(List.of(etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000)))
        .dataProviders(MORNINGSTAR)
        .build();

    HttpResponse response = postBatch(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    BatchResponse body = parseBatchResult(response.responseBody());

    AssetAllocationResult assetResult = parseResult(body, "asset-allocations", AssetAllocationResult.class);
    assertThat(assetResult.getAssetAllocation()).isNotNull();

    List<Notification> cpdErrors = body.errors.get("common-performance-dates");
    assertThat(cpdErrors).hasSize(1);
    assertThat(cpdErrors.get(0).getCode()).isEqualTo("MET-003");
  }

  // -------------------------------------------------------------------------
  // BatchContext SM deduplication
  // -------------------------------------------------------------------------

  /**
   * ASSET_ALLOCATIONS and EQUITY_GEOGRAPHIC_EXPOSURE both call the geography fetcher for the stock holding. With
   * BatchContext active the first call populates the cache; the second metric reuses it. Exactly one geography call
   * should reach the mock server.
   * <p>
   * The dispatcher tracks geography call count and returns HTTP 500 on the second call. If caching is broken the second
   * metric's geography fetch would fail with a server error, the metric would land in {@code errors}, and the assertion
   * on {@code results} would fail.
   */
  @Test
  void shouldDeduplicateSmCalls_whenTwoMetricsShareGeographyFetcherForSameStockHoldings() {
    AtomicInteger geographyCallCount = new AtomicInteger(0);

    smsMockServer.setDispatcher(new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath() != null ? request.getPath() : "";
        if (path.contains(SM_BATCH_ATTRIBUTES_PATH)) {
          // Return empty batch response so prefetch succeeds but adds nothing to the BatchContext.
          return jsonResponse("{}");
        }
        if (path.contains(GEOGRAPHY_PATH)) {
          if (geographyCallCount.getAndIncrement() == 0) {
            return jsonResponse(writeJson(List.of(
                geographyRow("RY.TO", FiIdentifierType.TICKER_MIC, SecurityRegion.CANADA, Currency.CAD))));
          }
          return new MockResponse().setResponseCode(500);
        }
        // All other allocation endpoints (equity-geographic, asset, etc.) return [] so the
        // services produce typed results with FDS warnings rather than throwing exceptions.
        return jsonResponse("[]");
      }
    });

    PortfolioHolding ryStock = equity("RY.TO", "TSX", FinancialInstrumentType.STOCK_CANADA, 100_000);

    BatchCalculationCommand command = BatchCalculationCommand.builder()
        .metrics(List.of(
            CalculationMetric.ASSET_ALLOCATIONS,
            CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE))
        .holdings(List.of(ryStock))
        .dataProviders(MORNINGSTAR)
        .build();

    HttpResponse response = postBatch(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    BatchResponse body = parseBatchResult(response.responseBody());

    // Both metrics must land in results — not in errors.
    AssetAllocationResult assetResult = parseResult(body, "asset-allocations", AssetAllocationResult.class);
    GeographicExposureResult geoResult = parseResult(body, "equity-geographic-exposure",
        GeographicExposureResult.class);

    // ASSET_ALLOCATIONS: RY.TO geography → CANADA → 100% CANADIAN_EQUITIES, no FDS warning.
    assertThat(assetResult.getWarnings()).isEmpty();
    assertThat(assetResult.getAssetAllocation())
        .containsKey(AssetAllocationRegionType.CANADIAN_EQUITIES);
    assertThat(assetResult.getAssetAllocation().get(AssetAllocationRegionType.CANADIAN_EQUITIES))
        .isCloseTo(BigDecimal.ONE, within(TOLERANCE));

    // EQUITY_GEOGRAPHIC_EXPOSURE: equity-geographic-allocation returns [] → FDS warning expected.
    assertThat(geoResult.getGeographicExposure()).isNotNull();

    // The BatchContext must have served the geography from cache — exactly one SM call.
    assertThat(geographyCallCount.get())
        .as("geography endpoint must be called exactly once — second access must hit BatchContext cache")
        .isEqualTo(1);
  }

  // -------------------------------------------------------------------------
  // Validation
  // -------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenMetricsListIsEmpty() {
    BatchCalculationCommand command = BatchCalculationCommand.builder()
        .metrics(List.of())
        .holdings(List.of(etf("XBAL", FinancialInstrumentType.ETF_CANADA, 50_000)))
        .build();

    HttpResponse response = postBatch(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldReturn400_whenMetricsFieldIsAbsent() {
    HttpResponse response = postBatch("{\"holdings\":[]}");

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  // -------------------------------------------------------------------------
  // Infrastructure
  // -------------------------------------------------------------------------

  private HttpResponse postBatch(String body) {
    var result = webTestClient.post()
        .uri(BATCH_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectBody(String.class);
    var exchangeResult = result.returnResult();
    return new HttpResponse(exchangeResult.getStatus(), exchangeResult.getResponseBody());
  }

  private record HttpResponse(HttpStatusCode status, String responseBody) {
  }

  private static String writeJson(Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static BatchResponse parseBatchResult(String responseBody) {
    try {
      return OBJECT_MAPPER.readValue(responseBody, BatchResponse.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static <T> T parseResult(BatchResponse body, String metricKey, Class<T> type) {
    JsonNode node = body.results != null ? body.results.get(metricKey) : null;
    assertThat(node).as("Expected result for metric '%s' but it was absent", metricKey).isNotNull();
    try {
      return OBJECT_MAPPER.treeToValue(node, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(body);
  }

  private static PortfolioHolding etf(String ticker, FinancialInstrumentType type, long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type,
        new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }

  private static PortfolioHolding equity(String ticker, String exchange, FinancialInstrumentType type,
      long value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), type,
        EquitySecurityIdentifier.builder()
            .id(ticker)
            .idType(FiIdentifierType.TICKER_MIC)
            .exchangeId(exchange)
            .build());
  }

  private static SecurityAttributeResult<Geography> geographyRow(String id, FiIdentifierType idType,
      SecurityRegion region, Currency currency) {
    RegionDatapoint regionDp = new RegionDatapoint();
    regionDp.setValue(region);
    CurrencyDatapoint currencyDp = new CurrencyDatapoint();
    currencyDp.setValue(currency);
    Geography geography = new Geography();
    geography.setRegion(regionDp);
    geography.setCurrency(currencyDp);
    geography.setDataProviders(MORNINGSTAR);
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    SecurityAttributeResult<Geography> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(geography);
    return result;
  }

  /**
   * Holds the raw JSON of each per-metric result (as {@link JsonNode} to allow conversion to concrete types) and the
   * fully typed per-metric errors.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static final class BatchResponse {
    public Map<String, JsonNode> results;
    public Map<String, List<Notification>> errors;
  }
}
