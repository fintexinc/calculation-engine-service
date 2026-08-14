package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.EquitySectorData;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.ce.model.error.ErrorParams.HOLDING_ID;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.ENERGY;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.FINANCIAL_SERVICES;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.TECHNOLOGY;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.UNKNOWN;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquitySectorExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final EquitySectorExposureService service = new EquitySectorExposureService(
      new PortfolioWeightCalculator(new HoldingCurrencyConverter(fxRateService, new FxProperties())));

  @Test
  void shouldAggregateSectorDistribution_whenHoldingIsFund() {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of(TECHNOLOGY, new BigDecimal("0.7"),
        FINANCIAL_SERVICES, new BigDecimal("0.3"))));

    var result = service.perform(command(fund), distributions(data));

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.7");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.3");
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * TMI-475: a stock arrives on the scalar EQUITY_SECTOR attribute and is widened to a single 100% bucket by the
   * adapter, so it flows through the fund path unchanged and contributes fully to its one sector.
   */
  @Test
  void shouldContributeFullWeightToOneSector_whenHoldingIsStockWithSingleBucket() {
    var stock = stock("T", "100");
    var data = Map.of(stock, sector(Currency.CAD, Map.of(ENERGY, ONE)));

    var result = service.perform(command(stock), distributions(data));

    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("1");
    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("0");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldBlendByValue_whenPortfolioMixesFundAndStock() {
    var fund = fund("RBF605", "100");
    var stock = stock("T", "100");
    var data = Map.of(
        fund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        stock, sector(Currency.CAD, Map.of(ENERGY, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(fund, stock), distributions(data));

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.5");
    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("0.5");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWarnAndBucketUnknown_whenStockSectorMissing() {
    var stock = stock("T", "100");

    var result = service.perform(command(stock), distributions(Map.of()));

    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).extracting("code").containsExactly(SECURITY_NOT_FOUND_FOR_METRIC.getCode());
  }

  @Test
  void shouldWarnAndBucketUnknown_whenFundAllocationsAreEmpty() {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of()));

    var result = service.perform(command(fund), distributions(data));

    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_EQUITY_SECTOR_ALLOCATION.getCode());
  }

  @Test
  void shouldReturnAllNullBuckets_whenOnlyCashAndGicHoldings() {
    var cash = CashHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD).build();
    var gic = GicHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.GIC)
        .currency(Currency.CAD).build();

    var result = service.perform(command(cash, gic), distributions(Map.of()));

    assertThat(result.getEquitySector().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("cashAndGicHoldings")
  void shouldExcludeHoldingFromWeighting_whenHoldingIsCashOrGic(PortfolioHolding excluded) {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(excluded, fund), distributions(data));

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("1");
    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("0");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldFxWeightSectors_whenHoldingsHaveDifferentCurrencies() {
    var cadFund = fund("CAD-1", "100");
    var usdFund = fund("USD-1", "100");
    var data = Map.of(
        cadFund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sector(Currency.USD, Map.of(FINANCIAL_SERVICES, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    var result = service.perform(command(cadFund, usdFund), distributions(data));

    // cad=100 CAD, usd=100 USD * 1.5 = 150 CAD -> weights 0.4 / 0.6
    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.4");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.6");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWeightByRawValueAndWarn_whenFxRateUnavailable() {
    var cadFund = fund("CAD-1", "100");
    var usdFund = fund("USD-1", "100");
    var data = Map.of(
        cadFund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sector(Currency.USD, Map.of(FINANCIAL_SERVICES, ONE)));
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    var result = service.perform(command(cadFund, usdFund), distributions(data));

    // no USD rate -> both weighted by raw value: 100/200 = 0.5 each
    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.5");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.5");
    assertThat(result.getWarnings()).extracting("code").containsExactly(FX_RATES_UNAVAILABLE.getCode());
  }

  static Stream<PortfolioHolding> cashAndGicHoldings() {
    return Stream.of(
        CashHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.CASH)
            .currency(Currency.CAD).build(),
        GicHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.GIC)
            .currency(Currency.CAD).build());
  }

  /**
   * The two attributes the metric asks for, as Security Master actually answers them for a stock: its sector on
   * EQUITY_SECTOR, and EQUITY_SECTOR_ALLOCATION present but empty, because that attribute is answered by every security
   * declaring any of its columns and each one declares {@code currency}. The empty distribution must not displace the
   * sector.
   */
  @Test
  void shouldKeepTheScalarSector_whenTheDistributionAttributeAnswersWithNoBuckets() {
    var stock = stock("T", "100");
    var securityData = SecurityData.builder()
        .with(CompositeSecurityAttribute.EQUITY_SECTOR, Map.of(stock, sector(Currency.CAD, Map.of(ENERGY, ONE))))
        .with(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION, Map.of(stock, sector(Currency.CAD, Map.of())))
        .build();

    var result = service.perform(command(stock), service.prepareData(securityData));

    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldPreferTheDistribution_whenASecurityAnswersBothAttributesWithData() {
    var fund = fund("RBF605", "100");
    var securityData = SecurityData.builder()
        .with(CompositeSecurityAttribute.EQUITY_SECTOR, Map.of(fund, sector(Currency.CAD, Map.of(ENERGY, ONE))))
        .with(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION,
            Map.of(fund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE))))
        .build();

    var result = service.perform(command(fund), service.prepareData(securityData));

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("1");
    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("0");
  }

  @Test
  void shouldAskForBothTheDistributionAndTheScalarSector() {
    assertThat(service.requiredAttributes()).containsExactly(
        CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION, CompositeSecurityAttribute.EQUITY_SECTOR);
  }

  /**
   * The other side of the merge rule: a holding Security Master answered for, but with no buckets and no scalar sector
   * either, must stay in the prepared map. Dropping it would turn "resolved, nothing distributed" into "not found", and
   * the client would be told the wrong thing about its own data.
   */
  @Test
  void shouldReportUndistributedRatherThanUnresolved_whenOnlyAnEmptyDistributionCameBack() {
    var fund = fund("RBF605", "100");
    var securityData = SecurityData.ofAttribute(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION,
        Map.of(fund, sector(Currency.CAD, Map.of())));

    var result = service.perform(command(fund), service.prepareData(securityData));

    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(MISSING_EQUITY_SECTOR_ALLOCATION.getCode());
      assertThat(warning.getMessage())
          .isEqualTo(MISSING_EQUITY_SECTOR_ALLOCATION.getFormattedMessage(fund.getIdsString()));
      assertThat(warning.getMetadata()).containsEntry(HOLDING_ID, fund.getIdsString());
    });
  }

  /**
   * Input carrying only the distribution attribute — the shape of every scenario that is not about which of the two
   * attributes answered. Those go through {@code prepareData} instead, so the merge rule is exercised where it lives
   * rather than restated by hand here.
   */
  private static EquitySectorData distributions(Map<PortfolioHolding, EquitySector> distributions) {
    return new EquitySectorData(distributions, Map.of());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static PortfolioHolding fund(String id, String value) {
    return holding(FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, id, value);
  }

  private static PortfolioHolding stock(String id, String value) {
    return holding(FinancialInstrumentType.STOCK, Country.CANADA, id, value);
  }

  private static PortfolioHolding holding(FinancialInstrumentType type, Country country, String id, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(type)
        .country(country)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .build();
  }

  private static EquitySector sector(Currency currency, Map<EquitySectorAllocationType, BigDecimal> allocations) {
    return new EquitySector(allocations, currency);
  }
}
