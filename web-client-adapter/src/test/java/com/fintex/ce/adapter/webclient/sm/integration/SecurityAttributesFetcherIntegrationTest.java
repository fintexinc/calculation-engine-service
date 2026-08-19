package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.ManagementFeeDatapoint;
import com.fintex.wm.commons.domain.holding.Holdings;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.rates.DateRateValue;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;
import com.fintex.wm.commons.domain.value.MultilingualString;
import com.fintex.wm.commons.dto.request.CompositeAttributesRequest;
import com.fintex.wm.commons.dto.search.TypedIdentifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * HTTP-level test of the generic security-attributes fetcher against a mocked SMS endpoint, exercising the full round
 * trip through the {@code FEES} attribute — whose mapper converts percentage form ({@code 1.51}) to ratio form
 * ({@code 0.0151}), so the assertions prove real conversion and identifier matching rather than just a returned type.
 */
@Tag("integration")
@SpringBootTest(classes = SecurityMasterWebClientIntegrationTestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SecurityAttributesFetcherIntegrationTest {

  private static final String ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes";

  /**
   * Spring WebFlux's out-of-the-box {@code maxInMemorySize}. A holding serialises to roughly 610 bytes, so a security
   * at Security Master's 1,000-holding cap is about 610 KB on its own, and the composite fetcher batches a whole
   * portfolio into a single request. The SM WebClient therefore raises this limit; without that, a body of this size
   * fails to decode and the adapter reports SMS as unavailable.
   */
  private static final int SPRING_DEFAULT_MAX_IN_MEMORY_SIZE = 262144;

  /** Enough unrequested fee rows to push the response body past {@link #SPRING_DEFAULT_MAX_IN_MEMORY_SIZE}. */
  private static final int FILLER_ROW_COUNT = 2000;

  private static MockWebServer smsMockServer;

  @Autowired
  private SecurityAttributesFetcher securityAttributesFetcher;

  @Autowired
  private TreasuryBillsFetcher treasuryBillsFetcher;

  @Autowired
  private ObjectMapper objectMapper;

  private static void ensureSmsMockServerStarted() throws IOException {
    if (smsMockServer == null) {
      smsMockServer = new MockWebServer();
      smsMockServer.start();
    }
  }

  @BeforeAll
  static void startSmsMockServerBeforeAll() throws IOException {
    ensureSmsMockServerStarted();
  }

  @AfterAll
  static void shutdownSmsMockServer() throws IOException {
    if (smsMockServer != null) {
      smsMockServer.shutdown();
      smsMockServer = null;
    }
  }

  private static String smsMockBaseUrl() {
    try {
      ensureSmsMockServerStarted();
      return smsMockServer.url("/").toString().replaceAll("/$", "");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.security-master.rest.base-url",
        SecurityAttributesFetcherIntegrationTest::smsMockBaseUrl);
  }

  @Test
  void shouldPostCompositeRequestAndMapFeesToRatios_whenCompositeFetch() throws Exception {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.FEES, List.of(
        feeRow("XIU", "1.51", DataProvider.MORNINGSTAR, "2.25", Currency.CAD))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(holding), List.of(CompositeSecurityAttribute.FEES), List.of(DataProvider.MORNINGSTAR));

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath()).isEqualTo(ATTRIBUTES_PATH);

    CompositeAttributesRequest request = objectMapper.readValue(recorded.getBody().readUtf8(),
        CompositeAttributesRequest.class);
    assertThat(request.getAttributes()).containsExactly(CompositeSecurityAttribute.FEES);
    assertThat(request.getDataProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(request.getTypedIdentifiers())
        .flatExtracting(TypedIdentifiers::getIds)
        .extracting(SecurityIdentifier::getId)
        .containsExactly("XIU");

    Map<PortfolioHolding, FeeData> fees = securityData.get(CompositeSecurityAttribute.FEES);
    assertThat(fees).containsOnlyKeys(holding);
    FeeData feeData = fees.get(holding);
    assertThat(feeData.getManagementFee()).isEqualByComparingTo("0.0151");
    assertThat(feeData.getManagementFeeProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(feeData.getManagementExpenseRatio()).isEqualByComparingTo("0.0225");
    assertThat(feeData.getCurrency()).isEqualTo(Currency.CAD);
  }

  @Test
  void shouldPostToAttributePathAndMapFees_whenSingleAttributeFetch() throws Exception {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    enqueueArrayResponse(List.of(feeRow("XIU", "0.80", DataProvider.FMP, "1.10", Currency.USD)));

    Map<PortfolioHolding, FeeData> result = securityAttributesFetcher.fetch(
        List.of(holding), CompositeSecurityAttribute.FEES, List.of(DataProvider.FMP));

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath())
        .isEqualTo(ATTRIBUTES_PATH + "/" + CompositeSecurityAttribute.FEES.getAttributeName());

    assertThat(result).containsOnlyKeys(holding);
    FeeData feeData = result.get(holding);
    assertThat(feeData.getManagementFee()).isEqualByComparingTo("0.0080");
    assertThat(feeData.getManagementFeeProvider()).isEqualTo(DataProvider.FMP);
    assertThat(feeData.getManagementExpenseRatio()).isEqualByComparingTo("0.0110");
    assertThat(feeData.getCurrency()).isEqualTo(Currency.USD);
  }

  @Test
  void shouldMapEachHoldingToItsOwnData_whenMultipleHoldingsFetched() throws Exception {
    PortfolioHolding xiu = etf("XIU", Country.CANADA, 1);
    PortfolioHolding vfv = etf("VFV", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.FEES, List.of(
        feeRow("XIU", "1.51", DataProvider.MORNINGSTAR, "2.25", Currency.CAD),
        feeRow("VFV", "0.09", DataProvider.MORNINGSTAR, "0.09", Currency.CAD))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(xiu, vfv), List.of(CompositeSecurityAttribute.FEES), List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, FeeData> fees = securityData.get(CompositeSecurityAttribute.FEES);
    assertThat(fees).containsOnlyKeys(xiu, vfv);
    assertThat(fees.get(xiu).getManagementFee()).isEqualByComparingTo("0.0151");
    assertThat(fees.get(vfv).getManagementFee()).isEqualByComparingTo("0.0009");
  }

  @Test
  void shouldIgnoreUnknownIdentifierRows_whenSmsReturnsUnrequestedHolding() throws Exception {
    PortfolioHolding requested = etf("XIU", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.FEES, List.of(
        feeRow("XIU", "1.51", DataProvider.MORNINGSTAR, "2.25", Currency.CAD),
        feeRow("UNKNOWN", "9.99", DataProvider.MORNINGSTAR, "9.99", Currency.CAD))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(requested), List.of(CompositeSecurityAttribute.FEES), List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, FeeData> fees = securityData.get(CompositeSecurityAttribute.FEES);
    assertThat(fees).containsOnlyKeys(requested);
    assertThat(fees.get(requested).getManagementFee()).isEqualByComparingTo("0.0151");
  }

  @Test
  void shouldNotIssueHttpRequest_whenHoldingsListIsEmpty() throws Exception {
    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(), List.of(CompositeSecurityAttribute.FEES), List.of(DataProvider.MORNINGSTAR));

    assertThat(securityData.asMap()).isEmpty();
    assertThat(smsMockServer.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
  }

  @Test
  void shouldReturnEmptySecurityData_whenSmsReturnsEmptyMap() throws Exception {
    smsMockServer.enqueue(new MockResponse()
        .setBody("{}")
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(etf("XIU", Country.CANADA, 1)), List.of(CompositeSecurityAttribute.FEES), List.of(
            DataProvider.MORNINGSTAR));

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getPath()).isEqualTo(ATTRIBUTES_PATH);
    assertThat(securityData.get(CompositeSecurityAttribute.FEES)).isEmpty();
  }

  @Test
  void shouldPreservePartialTreasuryBillSeries_whenSmsOmitsMonth() throws Exception {
    enqueueArrayResponse(List.of(
        new DateRateValue(LocalDate.of(2024, 1, 31), new BigDecimal("0.0035")),
        new DateRateValue(LocalDate.of(2024, 3, 31), new BigDecimal("0.0037"))));

    NavigableMap<LocalDate, BigDecimal> rates = treasuryBillsFetcher.fetch(Currency.CAD);

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("GET");
    assertThat(recorded.getPath()).isEqualTo("/api/v1/wealth/reference/treasury-rates/CAD");
    assertThat(rates).containsExactly(
        Map.entry(LocalDate.of(2024, 1, 31), new BigDecimal("0.0035")),
        Map.entry(LocalDate.of(2024, 3, 31), new BigDecimal("0.0037")));
    assertThat(rates).doesNotContainKey(LocalDate.of(2024, 2, 29));
  }

  @Test
  void shouldPreserveBenchmarkReturnGap_whenSmsOmitsMonth() throws Exception {
    PortfolioHolding benchmark = etf("BENCHMARK", Country.CANADA, 1);
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(List.of(
        dateValue("2024-01-31", "1.25"),
        dateValue("2024-03-31", "-0.75")));
    monthlyReturns.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.MONTHLY_RETURNS,
        List.of(attributeRow("BENCHMARK", monthlyReturns))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(benchmark), List.of(CompositeSecurityAttribute.MONTHLY_RETURNS),
        List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, HoldingMonthlyReturns> mapped = securityData.get(CompositeSecurityAttribute.MONTHLY_RETURNS);
    assertThat(mapped).containsOnlyKeys(benchmark);
    assertThat(mapped.get(benchmark).getReturns()).containsExactly(
        Map.entry(LocalDate.of(2024, 1, 31), new BigDecimal("1.25")),
        Map.entry(LocalDate.of(2024, 3, 31), new BigDecimal("-0.75")));
    assertThat(mapped.get(benchmark).getReturns()).doesNotContainKey(LocalDate.of(2024, 2, 29));
  }

  /**
   * The attribute behind top-common-holdings: proves the {@code LIMITED_HOLDINGS} binding is registered and that a
   * {@link Holdings} payload — plain holding rows plus the security's own currency — reaches the calculation as
   * {@link CommonTopHoldings} with weights converted to ratio form.
   */
  @Test
  void shouldMapLimitedHoldings_whenSmsReturnsAggregatedHoldings() throws Exception {
    PortfolioHolding etf = etf("AGGREGATED", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.LIMITED_HOLDINGS,
        List.of(attributeRow("AGGREGATED", limitedHoldings(Currency.CAD,
            securityHolding("NVIDIA Corp", "8.87516", "0P000003RE"),
            securityHolding("Microsoft Corp", "7.54319", "0P00000203"))))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(etf), List.of(CompositeSecurityAttribute.LIMITED_HOLDINGS), List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, CommonTopHoldings> mapped = securityData.<CommonTopHoldings>get(
        CompositeSecurityAttribute.LIMITED_HOLDINGS);
    assertThat(mapped).containsOnlyKeys(etf);
    assertThat(mapped.get(etf).getCurrency()).isEqualTo(Currency.CAD);
    assertThat(mapped.get(etf).getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(mapped.get(etf).getHoldings()).hasSize(2);

    CommonHolding nvidia = mapped.get(etf).getHoldings().getFirst();
    assertThat(nvidia.getName()).isEqualTo("NVIDIA Corp");
    assertThat(nvidia.getWeight()).isEqualByComparingTo("0.0887516");
    assertThat(nvidia.getPrimaryIdentifier().getId()).isEqualTo("0P000003RE");
    assertThat(nvidia.getUnderlyingHoldings()).isEmpty();
  }

  @Test
  void shouldPreserveHoldingsBeyondTopTwentyFive_whenSmsReturnsLimitedHoldings() throws Exception {
    PortfolioHolding etf = etf("ETF-WITH-MANY-HOLDINGS", Country.CANADA, 1);

    List<SecurityHolding> holdings = IntStream.rangeClosed(1, 41)
        .mapToObj(index -> securityHolding(
            "Holding " + index,
            String.valueOf(50 - index),
            "ID-" + index))
        .toList();

    enqueueArrayResponse(List.of(
        attributeRow(
            "ETF-WITH-MANY-HOLDINGS",
            limitedHoldings(
                Currency.CAD,
                holdings.toArray(SecurityHolding[]::new)))));

    Map<PortfolioHolding, CommonTopHoldings> mapped = securityAttributesFetcher.fetch(
        List.of(etf),
        CompositeSecurityAttribute.LIMITED_HOLDINGS,
        List.of(DataProvider.MORNINGSTAR));

    RecordedRequest recorded = smsMockServer.takeRequest();

    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath())
        .isEqualTo(
            ATTRIBUTES_PATH + "/"
                + CompositeSecurityAttribute.LIMITED_HOLDINGS.getAttributeName());

    assertThat(mapped).containsOnlyKeys(etf);

    assertThat(mapped.get(etf).getHoldings())
        .hasSize(41);

    assertThat(mapped.get(etf).getHoldings())
        .extracting(holding -> holding.getPrimaryIdentifier().getId())
        .containsExactly(
            IntStream.rangeClosed(1, 41)
                .mapToObj(index -> "ID-" + index)
                .toArray(String[]::new));
  }

  @Test
  void shouldPreserveNullHoldingFields_whenSmsOmitsCurrencyAndWeight() throws Exception {
    PortfolioHolding missingCurrency = etf("NO-CURRENCY", Country.CANADA, 1);
    PortfolioHolding missingWeight = etf("NO-WEIGHT", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.LIMITED_HOLDINGS,
        List.of(
            attributeRow("NO-CURRENCY", limitedHoldings(null, securityHolding("Equity", "5.0", null))),
            attributeRow("NO-WEIGHT",
                limitedHoldings(Currency.CAD, securityHolding("Missing Weight", null, "TREE"))))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(missingCurrency, missingWeight), List.of(CompositeSecurityAttribute.LIMITED_HOLDINGS),
        List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, CommonTopHoldings> holdings = securityData.<CommonTopHoldings>get(
        CompositeSecurityAttribute.LIMITED_HOLDINGS);
    assertThat(holdings).containsOnlyKeys(missingCurrency, missingWeight);
    assertThat(holdings.get(missingCurrency).getCurrency()).isNull();
    assertThat(holdings.get(missingCurrency).getHoldings()).hasSize(1);
    assertThat(holdings.get(missingCurrency).getHoldings().getFirst().getWeight()).isEqualByComparingTo("0.05");
    assertThat(holdings.get(missingWeight).getHoldings()).hasSize(1);
    assertThat(holdings.get(missingWeight).getHoldings().getFirst().getWeight()).isNull();
    assertThat(holdings.get(missingWeight).getHoldings().getFirst().getPrimaryIdentifier().getId()).isEqualTo("TREE");
  }

  @Test
  void shouldSkipFeeData_whenSmsOmitsHoldingRow() throws Exception {
    PortfolioHolding available = etf("FEE-AVAILABLE", Country.CANADA, 1);
    PortfolioHolding missing = etf("FEE-MISSING", Country.CANADA, 1);
    enqueueCompositeResponse(Map.of(CompositeSecurityAttribute.FEES, List.of(
        feeRow("FEE-AVAILABLE", "1.51", DataProvider.MORNINGSTAR, "2.25", Currency.CAD))));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(available, missing), List.of(CompositeSecurityAttribute.FEES),
        List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, FeeData> fees = securityData.get(CompositeSecurityAttribute.FEES);
    assertThat(fees).containsOnlyKeys(available);
    assertThat(fees).doesNotContainKey(missing);
    assertThat(fees.get(available).getManagementExpenseRatio()).isEqualByComparingTo("0.0225");
  }

  @Test
  void shouldDecodeResponse_whenPayloadExceedsTheFrameworkDefaultCodecLimit() throws Exception {
    PortfolioHolding requested = etf("XIU", Country.CANADA, 1);
    List<Map<String, Object>> rows = new ArrayList<>();
    rows.add(feeRow("XIU", "1.51", DataProvider.MORNINGSTAR, "2.25", Currency.CAD));
    IntStream.rangeClosed(1, FILLER_ROW_COUNT).forEach(index -> rows.add(
        feeRow("FILLER" + index, "0.10", DataProvider.MORNINGSTAR, "0.20", Currency.CAD)));

    String body = objectMapper.writeValueAsString(Map.of(CompositeSecurityAttribute.FEES, rows));
    assertThat(body.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(SPRING_DEFAULT_MAX_IN_MEMORY_SIZE);
    smsMockServer.enqueue(new MockResponse()
        .setBody(body)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    SecurityData securityData = securityAttributesFetcher.fetch(
        List.of(requested), List.of(CompositeSecurityAttribute.FEES), List.of(DataProvider.MORNINGSTAR));

    smsMockServer.takeRequest();
    Map<PortfolioHolding, FeeData> fees = securityData.get(CompositeSecurityAttribute.FEES);
    assertThat(fees).containsOnlyKeys(requested);
    assertThat(fees.get(requested).getManagementFee()).isEqualByComparingTo("0.0151");
  }

  private void enqueueCompositeResponse(Map<CompositeSecurityAttribute, List<Map<String, Object>>> body)
      throws Exception {
    smsMockServer.enqueue(new MockResponse()
        .setBody(objectMapper.writeValueAsString(body))
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
  }

  private void enqueueArrayResponse(List<?> body) throws Exception {
    smsMockServer.enqueue(new MockResponse()
        .setBody(objectMapper.writeValueAsString(body))
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
  }

  private Map<String, Object> feeRow(String id, String managementFeePercent, DataProvider managementFeeProvider,
      String managementExpenseRatioPercent, Currency currency) {
    Fees fees = new Fees();
    fees.setManagementFee(managementFeeDatapoint(managementFeePercent, managementFeeProvider));
    fees.setManagementExpenseRatio(floatDatapoint(managementExpenseRatioPercent));
    fees.setCurrency(currencyDatapoint(currency));
    return attributeRow(id, fees);
  }

  private Map<String, Object> attributeRow(String id, Object data) {
    return Map.of(
        "identifier", Map.of("id", id, "idType", FiIdentifierType.TICKER.name()),
        "data", objectMapper.convertValue(data, Map.class));
  }

  private static DateBigDecimalValue dateValue(String date, String value) {
    DateBigDecimalValue result = new DateBigDecimalValue();
    result.setDate(date);
    result.setValue(new BigDecimal(value));
    return result;
  }

  private static Holdings limitedHoldings(Currency currency, SecurityHolding... allocation) {
    Holdings holdings = new Holdings();
    holdings.setAllocation(List.of(allocation));
    holdings.setCurrency(currency);
    holdings.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return holdings;
  }

  private static SecurityHolding securityHolding(String name, String weighting, String identifier) {
    SecurityHolding holding = new SecurityHolding();
    holding.setName(List.of(new MultilingualString(LanguageCode.EN, name)));
    holding.setCompanyName(name);
    holding.setType("E");
    holding.setWeighting(weighting == null ? null : new BigDecimal(weighting));
    if (identifier != null) {
      IdentifiersDatapoint identifiers = new IdentifiersDatapoint();
      identifiers.setIdentifiers(List.of(new SecurityIdentifier(identifier, FiIdentifierType.MORNINGSTAR_ID)));
      holding.setIdentifiers(identifiers);
    }
    return holding;
  }

  private static ManagementFeeDatapoint managementFeeDatapoint(String percent, DataProvider provider) {
    ManagementFeeDatapoint datapoint = new ManagementFeeDatapoint();
    datapoint.setValue(new BigDecimal(percent));
    datapoint.setDataProviders(List.of(provider));
    return datapoint;
  }

  private static FloatDatapoint floatDatapoint(String percent) {
    FloatDatapoint datapoint = new FloatDatapoint();
    datapoint.setValue(new BigDecimal(percent));
    return datapoint;
  }

  private static CurrencyDatapoint currencyDatapoint(Currency currency) {
    CurrencyDatapoint datapoint = new CurrencyDatapoint();
    datapoint.setValue(currency);
    return datapoint;
  }

}
