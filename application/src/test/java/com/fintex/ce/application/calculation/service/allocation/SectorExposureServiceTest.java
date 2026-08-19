package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.HoldingSectorAllocation;
import com.fintex.ce.model.domain.calculation.allocation.SectorExposureData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.SectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.ce.model.error.ErrorParams.HOLDING_ID;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.gic;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.CORPORATE_BONDS;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.ENERGY;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.GOVERNMENT_BONDS;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.OTHER;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.OTHER_BONDS;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.ST_INVESTMENTS;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.TECHNOLOGY;
import static com.fintex.wm.commons.domain.allocation.SectorAllocationType.UNKNOWN;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Since TMI-558 the sleeve reconciliation happens in Security Master, which publishes one vector per security summing
 * to 1. What is left for this service, and what these tests cover, is everything Security Master cannot do: holdings it
 * has no security for (cash, GICs), holdings it returned nothing for, FX weighting across currencies, and the
 * portfolio-level invariant that the buckets total 100% without a final rescale.
 */
class SectorExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final SectorExposureService service = new SectorExposureService(
      new PortfolioWeightCalculator(new HoldingCurrencyConverter(fxRateService, new FxProperties())));

  /**
   * The consolidated vector already carries each sleeve's share of the security, so a balanced fund's stock sectors and
   * bond buckets reach the portfolio in the proportions the provider stated — the defect the per-sleeve metrics have,
   * where the equity profile is applied to the bond half as well, cannot arise here.
   */
  @Test
  void shouldContributeVendorProportions_whenFundIsBalanced() {
    var fund = holding(new SecurityIdentifier("BAL-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund, sectors(Map.of(
        TECHNOLOGY, new BigDecimal("0.6"),
        GOVERNMENT_BONDS, new BigDecimal("0.2"),
        CORPORATE_BONDS, new BigDecimal("0.2"))));

    var result = service.perform(command(fund), distributions(data));

    assertDistribution(result, Map.of(
        TECHNOLOGY, "0.6",
        GOVERNMENT_BONDS, "0.2",
        CORPORATE_BONDS, "0.2"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * A pure bond fund contributes only bond sectors and, unlike the per-sleeve equity metric, produces no UNKNOWN slice:
   * having no equity is not missing data.
   */
  @Test
  void shouldReportNoUnknownExposure_whenFundHoldsNoEquity() {
    var fund = holding(new SecurityIdentifier("BOND-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund, sectors(Map.of(GOVERNMENT_BONDS, ONE)));

    var result = service.perform(command(fund), distributions(data));

    assertDistribution(result, Map.of(GOVERNMENT_BONDS, "1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * Exposure Security Master could not put a sector on travels as its own bucket rather than being spread over the
   * sectors that did resolve, and reaches the client donut unchanged.
   */
  @Test
  void shouldPreserveUnsectoredAndUnexplainedShares_whenVendorReportsThem() {
    var fund = holding(new SecurityIdentifier("ALT-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund, sectors(Map.of(
        TECHNOLOGY, new BigDecimal("0.5"),
        OTHER, new BigDecimal("0.3"),
        UNKNOWN, new BigDecimal("0.2"))));

    var result = service.perform(command(fund), distributions(data));

    assertDistribution(result, Map.of(
        TECHNOLOGY, "0.5",
        OTHER, "0.3",
        UNKNOWN, "0.2"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * Security Master balances every published vector to exactly 1, but this metric reports its buckets without a final
   * rescale, so a vector that arrived summing to more than 1 would push the portfolio past 100% with nothing downstream
   * to catch it. The local rescale keeps that invariant independent of the upstream release in production.
   */
  @Test
  void shouldContributeExactlyOneWholeHolding_whenVendorDistributionDoesNotSumToOne() {
    var fund = holding(new SecurityIdentifier("BAL-5", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund, sectors(Map.of(
        TECHNOLOGY, new BigDecimal("0.8"),
        ENERGY, new BigDecimal("0.4"),
        GOVERNMENT_BONDS, new BigDecimal("0.4"))));

    var result = service.perform(command(fund), distributions(data));

    assertDistribution(result, Map.of(
        TECHNOLOGY, "0.5",
        ENERGY, "0.25",
        GOVERNMENT_BONDS, "0.25"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * Once the datum has arrived, a stock is one bucket and needs no handling of its own — the same shape a fund's
   * distribution has, with all the weight on one entry. How it arrives is the pair of tests below: Security Master
   * publishes a company's sector on the scalar {@code EQUITY_SECTOR} attribute, not in the consolidated column, which
   * it fills for composite securities only.
   */
  @Test
  void shouldContributeFullWeightToOneSector_whenHoldingIsStock() {
    var stock = holding(new SecurityIdentifier("T", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, "100");
    var data = Map.of(stock, sectors(Map.of(ENERGY, ONE)));

    var result = service.perform(command(stock), distributions(data));

    assertDistribution(result, Map.of(ENERGY, "1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({"180, ST_INVESTMENTS", "1095, OTHER_BONDS"})
  void shouldBucketGicByTerm_whenHoldingIsGic(int termDays, SectorAllocationType expected) {
    var gic = gic(null, Currency.CAD, new BigDecimal("100"), new BigDecimal(termDays));

    var result = service.perform(command(gic), distributions(Map.of()));

    assertDistribution(result, Map.of(expected, "1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldBucketCashAsShortTermInvestments_whenHoldingIsCash() {
    var cash = cash(Currency.CAD, "100");

    var result = service.perform(command(cash), distributions(Map.of()));

    assertDistribution(result, Map.of(ST_INVESTMENTS, "1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWarnSecurityNotFoundAndBucketUnknown_whenSecurityMasterHasNoRecord() {
    var fund = holding(new SecurityIdentifier("GHOST-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");

    var result = service.perform(command(fund), distributions(Map.of()));

    assertDistribution(result, Map.of(UNKNOWN, "1"));
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(SECURITY_NOT_FOUND_FOR_METRIC.getCode());
      assertThat(warning.getMessage()).isEqualTo(SECURITY_NOT_FOUND_FOR_METRIC
          .getFormattedMessage(fund.getIdsString(), CalculationMetric.SECTOR_EXPOSURE.getUserFriendlyName()));
      assertThat(warning.getMetadata())
          .containsEntry(HOLDING_ID, fund.getIdsString())
          .containsEntry("param-1", fund.getIdsString())
          .containsEntry("param-2", CalculationMetric.SECTOR_EXPOSURE.getUserFriendlyName());
    });
  }

  /**
   * Resolved but without a distribution is a different fact from not resolved at all, and gets its own warning: the
   * security exists, the data provider simply published no sector breakdown for it.
   */
  @Test
  void shouldWarnMissingSectorAllocationAndBucketUnknown_whenSecurityResolvesWithoutDistribution() {
    var fund = holding(new SecurityIdentifier("EMPTY-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund, sectors(Map.of()));

    var result = service.perform(command(fund), distributions(data));

    assertDistribution(result, Map.of(UNKNOWN, "1"));
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(MISSING_SECTOR_ALLOCATION.getCode());
      assertThat(warning.getMessage())
          .isEqualTo(MISSING_SECTOR_ALLOCATION.getFormattedMessage(fund.getIdsString()));
      assertThat(warning.getMetadata())
          .containsEntry(HOLDING_ID, fund.getIdsString())
          .containsEntry("param-1", fund.getIdsString());
    });
  }

  @Test
  void shouldReturnAllNullBuckets_whenPortfolioHasNoHoldings() {
    var result = service.perform(command(), distributions(Map.of()));

    assertThat(result.getSectorExposure()).containsOnlyKeys(SectorAllocationType.values());
    assertThat(result.getSectorExposure().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldFxWeightSectors_whenHoldingsHaveDifferentCurrencies() {
    var cadFund = holding(new SecurityIdentifier("CAD-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var usdFund = holding(new SecurityIdentifier("USD-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(
        cadFund, sectors(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sectors(Currency.USD, Map.of(GOVERNMENT_BONDS, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    var result = service.perform(command(cadFund, usdFund), distributions(data));

    // cad=100 CAD, usd=100 USD * 1.5 = 150 CAD -> weights 0.4 / 0.6
    assertDistribution(result, Map.of(
        TECHNOLOGY, "0.4",
        GOVERNMENT_BONDS, "0.6"));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWeightByRawValueAndWarn_whenFxRateUnavailable() {
    var cadFund = holding(new SecurityIdentifier("CAD-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var usdFund = holding(new SecurityIdentifier("USD-1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(
        cadFund, sectors(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sectors(Currency.USD, Map.of(GOVERNMENT_BONDS, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(cadFund, usdFund), distributions(data));

    assertDistribution(result, Map.of(
        TECHNOLOGY, "0.5",
        GOVERNMENT_BONDS, "0.5"));
    assertThat(result.getWarnings()).extracting("code").containsExactly(FX_RATES_UNAVAILABLE.getCode());
  }

  /**
   * The invariant the client donut depends on: every holding type in one portfolio, and the buckets still total 100%
   * without the metric rescaling anything.
   */
  @Test
  void shouldTotalWholePortfolio_whenPortfolioMixesEveryHoldingType() {
    var stock = holding(new SecurityIdentifier("T", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, "100");
    var balanced = holding(new SecurityIdentifier("BAL-4", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "200");
    var bondFund = holding(new SecurityIdentifier("BOND-4", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var cash = cash(Currency.CAD, "50");
    var gic = gic(null, Currency.CAD, new BigDecimal("50"), new BigDecimal("1095"));
    var data = Map.of(
        stock, sectors(Map.of(ENERGY, ONE)),
        balanced, sectors(Map.of(TECHNOLOGY, new BigDecimal("0.5"), CORPORATE_BONDS, new BigDecimal("0.5"))),
        bondFund, sectors(Map.of(GOVERNMENT_BONDS, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(stock, balanced, bondFund, cash, gic), distributions(data));

    // weights: stock 0.2, balanced 0.4, bond fund 0.2, cash 0.1, gic 0.1
    assertDistribution(result, Map.of(
        ENERGY, "0.2",
        TECHNOLOGY, "0.2",
        CORPORATE_BONDS, "0.2",
        GOVERNMENT_BONDS, "0.2",
        ST_INVESTMENTS, "0.1",
        OTHER_BONDS, "0.1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * The stock path as Security Master actually answers it: the company's sector on the scalar attribute, translated
   * from the equity taxonomy onto the consolidated one, and the consolidated attribute answering with the currency
   * alone because it is filled for composite securities only. The empty consolidated row must not displace the sector —
   * that is what used to send every individual stock into {@code UNKNOWN} and leave the pie short of the money in it.
   */
  @Test
  void shouldBucketTheStockByItsScalarSector_whenTheConsolidatedAttributeIsEmptyForIt() {
    var stock = holding(new SecurityIdentifier("T", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, "100");
    var securityData = SecurityData.builder()
        .with(CompositeSecurityAttribute.EQUITY_SECTOR,
            Map.of(stock, equitySector(EquitySectorAllocationType.ENERGY)))
        .with(CompositeSecurityAttribute.SECTOR_ALLOCATION, Map.of(stock, sectors(Map.of())))
        .build();

    var result = service.perform(command(stock), service.prepareData(securityData));

    assertDistribution(result, Map.of(ENERGY, "1"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * A fund keeps its consolidated distribution: it is the richer statement, and it is the one that carries each
   * sleeve's share of the security rather than the equity sleeve alone.
   */
  @Test
  void shouldKeepTheConsolidatedDistribution_whenBothAttributesAnswerForAFund() {
    var fund = holding(new SecurityIdentifier("RBF605", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var securityData = SecurityData.builder()
        .with(CompositeSecurityAttribute.EQUITY_SECTOR,
            Map.of(fund, equitySector(EquitySectorAllocationType.ENERGY)))
        .with(CompositeSecurityAttribute.SECTOR_ALLOCATION,
            Map.of(fund, sectors(Map.of(TECHNOLOGY, new BigDecimal("0.6"), GOVERNMENT_BONDS, new BigDecimal("0.4")))))
        .build();

    var result = service.perform(command(fund), service.prepareData(securityData));

    assertDistribution(result, Map.of(TECHNOLOGY, "0.6", GOVERNMENT_BONDS, "0.4"));
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * A scalar the consolidated taxonomy cannot name leaves the holding with nothing distributed, so it is reported as
   * resolved-but-undistributed rather than silently bucketed as {@code UNKNOWN} with no warning. Defensive — every real
   * equity sector has a counterpart — but the difference is a warning the client either gets or does not.
   */
  @Test
  void shouldWarnRatherThanBucketUnknown_whenTheScalarSectorHasNoConsolidatedCounterpart() {
    var stock = holding(new SecurityIdentifier("T", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, "100");
    var securityData = SecurityData.ofAttribute(CompositeSecurityAttribute.EQUITY_SECTOR,
        Map.of(stock, equitySector(EquitySectorAllocationType.UNKNOWN)));

    var result = service.perform(command(stock), service.prepareData(securityData));

    assertDistribution(result, Map.of(UNKNOWN, "1"));
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(MISSING_SECTOR_ALLOCATION.getCode());
      assertThat(warning.getMessage())
          .isEqualTo(MISSING_SECTOR_ALLOCATION.getFormattedMessage(stock.getIdsString()));
      assertThat(warning.getMetadata()).containsEntry(HOLDING_ID, stock.getIdsString());
    });
  }

  @Test
  void shouldAskForTheConsolidatedDistributionAndTheScalarSector() {
    assertThat(service.requiredAttributes()).containsExactly(
        CompositeSecurityAttribute.SECTOR_ALLOCATION, CompositeSecurityAttribute.EQUITY_SECTOR);
  }

  private static EquitySector equitySector(EquitySectorAllocationType sector) {
    return EquitySector.builder()
        .allocations(Map.of(sector, ONE))
        .currency(Currency.CAD)
        .build();
  }

  /**
   * Asserts the whole distribution rather than the buckets a scenario happens to populate: every bucket of the enum is
   * present, the listed ones carry exactly the expected value, every other one is exactly zero, and the buckets total
   * 100%. A bucket quietly picking up weight — the equity profile bleeding onto a bond half — is the defect this metric
   * exists to fix, so the zeros are part of the expectation rather than an afterthought.
   *
   * <p>
   * Exact rather than within a tolerance: the metric deliberately does not renormalise, so every expectation here is a
   * weighted sum that terminates in base 10, and a rounding regression should fail rather than slip under a tolerance.
   */
  private static void assertDistribution(ConsolidatedSectorExposureResult result,
      Map<SectorAllocationType, String> expected) {
    Map<SectorAllocationType, BigDecimal> actual = result.getSectorExposure();
    assertThat(actual).containsOnlyKeys(SectorAllocationType.values());
    for (SectorAllocationType bucket : SectorAllocationType.values()) {
      assertThat(actual.get(bucket)).as("bucket %s", bucket)
          .isEqualByComparingTo(new BigDecimal(expected.getOrDefault(bucket, "0")));
    }
    assertThat(actual.values().stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add))
        .isEqualByComparingTo(ONE);
  }

  /**
   * Input carrying only the consolidated attribute — the shape of every scenario that is not about which of the two
   * attributes answered. Those go through {@code prepareData} instead, so the merge rule and the taxonomy translation
   * are exercised where they live rather than restated by hand here.
   */
  private static SectorExposureData distributions(Map<PortfolioHolding, HoldingSectorAllocation> distributions) {
    return new SectorExposureData(distributions, Map.of());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static HoldingSectorAllocation sectors(Map<SectorAllocationType, BigDecimal> allocations) {
    return sectors(Currency.CAD, allocations);
  }

  private static HoldingSectorAllocation sectors(Currency currency,
      Map<SectorAllocationType, BigDecimal> allocations) {
    return HoldingSectorAllocation.builder()
        .allocations(allocations)
        .currency(currency)
        .build();
  }
}
