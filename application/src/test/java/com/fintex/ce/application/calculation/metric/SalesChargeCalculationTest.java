package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.enumeration.SalesChargeCategory;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.sales.SalesChargeType;

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
import static com.fintex.wm.commons.domain.sales.SalesChargeType.DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.DEFERRED_SALES_CHARGE_ON_MARKET_VALUE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.FORMULA_ONE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.FRONT_END_CHARGE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.LOW_SALES_CHARGE;
import static com.fintex.wm.commons.domain.sales.SalesChargeType.VOLUME_SALES_CHARGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    final PortfolioHolding holding1 = createHolding("RBF605", 10_000);
    final PortfolioHolding holding2 = createHolding("RBF606", 20_000);
    final PortfolioHolding holding3 = createHolding("RBF607", 70_000);
    dataFromFds.put(holding1, new SalesCharge(DEFERRED_SALES_CHARGE_ON_MARKET_VALUE));
    dataFromFds.put(holding2, new SalesCharge(FRONT_END_CHARGE));
    dataFromFds.put(holding3, new SalesCharge(LOW_SALES_CHARGE));

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var rbf605 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF605", scaled(0.10));
    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF606", scaled(0.20));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF607", scaled(0.70));

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.10), BigDecimal.valueOf(10_000), Set.of(rbf605));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.20), BigDecimal.valueOf(20_000), Set.of(rbf606));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.70), BigDecimal.valueOf(70_000), Set.of(rbf607));
    final var expected = new SalesChargeResult(Map.of(
        SalesChargeCategory.DEFERRED_SALES_CHARGE, s1,
        SalesChargeCategory.NO_LOAD_INITIAL_SALES_CHARGE, s2,
        SalesChargeCategory.LOW_LOAD_SALES_CHARGE, s3));
    final var actual = calculation.calculate();

    assertEquals(expected, actual);
  }

  private PortfolioHolding createHolding(final String fundServCode, final int value) {
    return new PortfolioHolding(BigDecimal.valueOf(value), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier(fundServCode, FiIdentifierType.FUNDSERV));
  }

  @Test
  void shouldCalculateWeights_whenTwoTypesContainOneHoldingEach() {

    final Map<PortfolioHolding, SalesCharge> dataFromFds = new HashMap<>();
    final PortfolioHolding holding2 = createHolding("RBF606", 51_000);
    final PortfolioHolding holding3 = createHolding("RBF607", 49_000);
    dataFromFds.put(holding2, new SalesCharge(VOLUME_SALES_CHARGE));
    dataFromFds.put(holding3, new SalesCharge(DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT));

    final var calculation = new SalesChargeCalculation(dataFromFds);

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF606", scaled(0.51));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF607", scaled(0.49));

    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.51), BigDecimal.valueOf(51_000), Set.of(rbf606));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.49), BigDecimal.valueOf(49_000), Set.of(rbf607));
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

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF606", scaled(0.10));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF607", scaled(0.15));
    final var rbf608 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF608", scaled(0.17));
    final var rbf609 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF609", scaled(0.13));
    final var rbf610 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF610", scaled(0.25));
    final var rbf611 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF611", scaled(0.20));

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.42), BigDecimal.valueOf(42_000), Set.of(rbf606,
        rbf607, rbf608));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.13), BigDecimal.valueOf(13_000), Set.of(rbf609));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.45), BigDecimal.valueOf(45_000), Set.of(rbf610,
        rbf611));
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

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF606", BigDecimal.valueOf(
        0.3333333333));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF607", BigDecimal.valueOf(
        0.3333333333));
    final var rbf608 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF608", BigDecimal.valueOf(
        0.3333333333));

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000),
        Set.of(rbf606, rbf607, rbf608));
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
    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("MUTUAL_FUND_CANADA-RBF606", BigDecimal.valueOf(
        1));

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000),
        Set.of(rbf606));
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
    final PortfolioHolding holding = new PortfolioHolding(BigDecimal.valueOf(value),
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier(fundServCode, FiIdentifierType.FUNDSERV));

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
