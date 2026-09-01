package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.enumeration.SalesChargeCategory;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.sales.SalesChargeType;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.application.calculation.metric.SalesChargeCalculation.DEFAULT_MAP;
import static com.fintex.ce.application.calculation.metric.SalesChargeCalculation.DEFAULT_SALES_CHARGE_DTO;
import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static com.fintex.ce.model.util.BigDecimalConstants.ROUNDING_MODE;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.DEFERRED_SALES_CHARGE_ON_MARKET_VALUE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.FORMULA_ONE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.FRONT_END_CHARGE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.LOW_SALES_CHARGE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.VOLUME_SALES_CHARGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("metric unsupported")
class SalesChargeTypeCalculationTest {

  private static BigDecimal scaled(double val) {
    return BigDecimal.valueOf(val).setScale(OUTPUT_SCALE, ROUNDING_MODE);
  }

  @Test
  void shouldReturnDefaultMap_whenSalesChargeDataIsEmpty() {
    final var calculation = new SalesChargeCalculation(Map.of());

    final var expected = new SalesChargeResult(DEFAULT_MAP);
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateWeightsPerType_whenEachTypeContainsOneHolding() {
    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    final PortfolioHolding holding1 = holding("RBF605", FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.valueOf(10_000));
    final PortfolioHolding holding2 = holding("RBF606", FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.valueOf(20_000));
    final PortfolioHolding holding3 = holding("RBF607", FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.valueOf(70_000));
    dataFromFds.put(holding1, new SalesCharge(DEFERRED_SALES_CHARGE_ON_MARKET_VALUE));
    dataFromFds.put(holding2, new SalesCharge(FRONT_END_CHARGE));
    dataFromFds.put(holding3, new SalesCharge(LOW_SALES_CHARGE));

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.10), BigDecimal.valueOf(10_000),
        Map.of("MUTUAL_FUND-RBF605", scaled(0.10)));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.20), BigDecimal.valueOf(20_000),
        Map.of("MUTUAL_FUND-RBF606", scaled(0.20)));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.70), BigDecimal.valueOf(70_000),
        Map.of("MUTUAL_FUND-RBF607", scaled(0.70)));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.DEFERRED_SALES_CHARGE, s1,
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s2,
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, s3));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateWeights_whenTwoTypesContainOneHoldingEach() {

    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    final PortfolioHolding holding2 = holding("RBF606", FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.valueOf(51_000));
    final PortfolioHolding holding3 = holding("RBF607", FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
        BigDecimal.valueOf(49_000));
    dataFromFds.put(holding2, new SalesCharge(VOLUME_SALES_CHARGE));
    dataFromFds.put(holding3, new SalesCharge(DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT));

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.51), BigDecimal.valueOf(51_000),
        Map.of("MUTUAL_FUND-RBF606", scaled(0.51)));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.49), BigDecimal.valueOf(49_000),
        Map.of("MUTUAL_FUND-RBF607", scaled(0.49)));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s2,
        SalesChargeCategory.DEFERRED_SALES_CHARGE, s3));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldAggregateHoldingsByType_whenEachTypeHasMultipleHoldings() {
    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 10_000, FRONT_END_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF607", 15_000, VOLUME_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF608", 17_000, FORMULA_ONE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF609", 13_000, LOW_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF610", 25_000, DEFERRED_SALES_CHARGE_ON_MARKET_VALUE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF611", 20_000, DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT);

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.42), BigDecimal.valueOf(42_000),
        Map.of("MUTUAL_FUND-RBF606", scaled(0.10), "MUTUAL_FUND-RBF607", scaled(0.15), "MUTUAL_FUND-RBF608",
            scaled(0.17)));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.13), BigDecimal.valueOf(13_000),
        Map.of("MUTUAL_FUND-RBF609", scaled(0.13)));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.45), BigDecimal.valueOf(45_000),
        Map.of("MUTUAL_FUND-RBF610", scaled(0.25), "MUTUAL_FUND-RBF611", scaled(0.20)));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, s2,
        SalesChargeCategory.DEFERRED_SALES_CHARGE, s3));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldReturnDefaultMap_whenNoHoldingsProvided() {
    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var expected = new SalesChargeResult(DEFAULT_MAP);
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateFullWeightForSingleType_whenThreeHoldingsShareSameType() {
    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 50_000, FRONT_END_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF607", 50_000, VOLUME_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF608", 50_000, FORMULA_ONE);

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000),
        Map.of("MUTUAL_FUND-RBF606", BigDecimal.valueOf(0.3333333333), "MUTUAL_FUND-RBF607", BigDecimal.valueOf(
            0.3333333333), "MUTUAL_FUND-RBF608", BigDecimal.valueOf(0.3333333333)));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesChargeCategory.DEFERRED_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateFullWeightForSingleType_whenOneHoldingSharesSameType() {
    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 150_000, FRONT_END_CHARGE);

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000),
        Map.of("MUTUAL_FUND-RBF606", BigDecimal.valueOf(1)));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesChargeCategory.DEFERRED_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  private void addHoldingAndRSalesCharge(final Map<PortfolioHolding, SalesCharge> dataFromFds,
      final String fundServCode,
      final int value,
      final SalesChargeType frontEndCharge) {
    final PortfolioHolding holding = holding(fundServCode, FiIdentifierType.FUNDSERV,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, BigDecimal.valueOf(value));

    dataFromFds.put(holding, new SalesCharge(frontEndCharge));
  }

  @Test
  void shouldMapAllSalesChargeTypesToCategory() {
    final Set<SalesChargeType> unmapped = Arrays.stream(SalesChargeType.values())
        .filter(type -> SalesChargeCategory.fromValue(type) == null)
        .collect(Collectors.toSet());

    assertTrue(unmapped.isEmpty(),
        "All SalesChargeType values must be mapped to a SalesChargeCategory, unmapped: " + unmapped);
  }

}
