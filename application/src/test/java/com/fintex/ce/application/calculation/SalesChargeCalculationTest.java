package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.SalesChargeCalculation;
import com.fintex.ce.domain.enumeration.calculation.SalesCharge;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.result.SalesChargeResult;
import com.fintex.smclient.graphql.SalesChargeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.application.calculation.SalesChargeCalculation.DEFAULT_MAP;
import static com.fintex.ce.application.calculation.SalesChargeCalculation.DEFAULT_SALES_CHARGE_DTO;
import static com.fintex.smclient.graphql.SalesChargeType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesChargeCalculationTest {

  private static final int OUTPUT_SCALE = 10;

  private static BigDecimal scaled(double val) {
    return BigDecimal.valueOf(val).setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);
  }

  @Test
  void calculate_salesChargeIsEmptyReturnEmptyResult() {
    // SETUP
    final var sut = new SalesChargeCalculation(Map.of());

    final var expected = new SalesChargeResult().setSalesCharges(DEFAULT_MAP);

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculate_checkResultEachTypeContainOneHolding() {
    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();
    final FundSeriesHolding holding1 = getFundSeriesHolding("RBF605", 10_000);
    final FundSeriesHolding holding2 = getFundSeriesHolding("RBF606", 20_000);
    final FundSeriesHolding holding3 = getFundSeriesHolding("RBF607", 70_000);
    dataFromFds.put(holding1, new com.fintex.ce.domain.model.SalesCharge(DEFERRED_SALES_CHARGE_ON_MARKET_VALUE.name(),
        null, null, null, null));
    dataFromFds.put(holding2, new com.fintex.ce.domain.model.SalesCharge(FRONT_END_CHARGE.name(), null, null, null,
        null));
    dataFromFds.put(holding3, new com.fintex.ce.domain.model.SalesCharge(LOW_SALES_CHARGE.name(), null, null, null,
        null));

    final var sut = new SalesChargeCalculation(dataFromFds);

    final var rbf605 = new SalesChargeResult.SalesChargeHoldingEntry("RBF605", scaled(0.10));
    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("RBF606", scaled(0.20));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("RBF607", scaled(0.70));

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.10), BigDecimal.valueOf(10_000), Set
        .of(rbf605));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.20), BigDecimal.valueOf(20_000), Set
        .of(rbf606));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.70), BigDecimal.valueOf(70_000), Set
        .of(rbf607));
    final var expected = new SalesChargeResult().setSalesCharges(Map.of(
        SalesCharge.DEFERRED_SALES_CHARGE, s1,
        SalesCharge.NO_LOAD_INITIAL_SALES_CHARGE, s2,
        SalesCharge.LOW_LOAD_SALES_CHARGE, s3));

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  private FundSeriesHolding getFundSeriesHolding(final String fundServCode, final int value) {
    final FundSeriesHolding holding = new FundSeriesHolding();
    holding.setFundServCode(fundServCode);
    holding.setValue(BigDecimal.valueOf(value));
    return holding;
  }

  @Test
  void calculate_checkResultTwoSalesChargesContainOneHoldingEach() {

    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();
    final FundSeriesHolding holding2 = getFundSeriesHolding("RBF606", 51_000);
    final FundSeriesHolding holding3 = getFundSeriesHolding("RBF607", 49_000);
    dataFromFds.put(holding2, new com.fintex.ce.domain.model.SalesCharge(VOLUME_SALES_CHARGE.name(), null, null, null,
        null));
    dataFromFds.put(holding3, new com.fintex.ce.domain.model.SalesCharge(DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT.name(),
        null, null, null, null));

    final var sut = new SalesChargeCalculation(dataFromFds);

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("RBF606", scaled(0.51));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("RBF607", scaled(0.49));

    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.51), BigDecimal.valueOf(51_000), Set
        .of(rbf606));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.49), BigDecimal.valueOf(49_000), Set
        .of(rbf607));
    final var expected = new SalesChargeResult().setSalesCharges(Map.of(
        SalesCharge.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesCharge.NO_LOAD_INITIAL_SALES_CHARGE, s2,
        SalesCharge.DEFERRED_SALES_CHARGE, s3));

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculate_checkResultWhenEachSalesChargeHaveFewHoldings() {
    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 10_000, FRONT_END_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF607", 15_000, VOLUME_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF608", 17_000, FORMULA_ONE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF609", 13_000, LOW_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF610", 25_000, DEFERRED_SALES_CHARGE_ON_MARKET_VALUE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF611", 20_000, DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT);

    final var sut = new SalesChargeCalculation(dataFromFds);

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("RBF606", scaled(0.10));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("RBF607", scaled(0.15));
    final var rbf608 = new SalesChargeResult.SalesChargeHoldingEntry("RBF608", scaled(0.17));
    final var rbf609 = new SalesChargeResult.SalesChargeHoldingEntry("RBF609", scaled(0.13));
    final var rbf610 = new SalesChargeResult.SalesChargeHoldingEntry("RBF610", scaled(0.25));
    final var rbf611 = new SalesChargeResult.SalesChargeHoldingEntry("RBF611", scaled(0.20));

    final var s1 = new SalesChargeResult.SalesChargeEntry(scaled(0.42), BigDecimal.valueOf(42_000), Set
        .of(rbf606, rbf607, rbf608));
    final var s2 = new SalesChargeResult.SalesChargeEntry(scaled(0.13), BigDecimal.valueOf(13_000), Set
        .of(rbf609));
    final var s3 = new SalesChargeResult.SalesChargeEntry(scaled(0.45), BigDecimal.valueOf(45_000), Set
        .of(rbf610, rbf611));
    final var expected = new SalesChargeResult().setSalesCharges(Map.of(
        SalesCharge.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesCharge.LOW_LOAD_SALES_CHARGE, s2,
        SalesCharge.DEFERRED_SALES_CHARGE, s3));

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculate_checkResultWhenEachSalesChargeDontHaveHoldings() {
    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();

    final var sut = new SalesChargeCalculation(dataFromFds);

    final var expected = new SalesChargeResult().setSalesCharges(DEFAULT_MAP);

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculate_checkResultWhenOneSalesChargeHaveThreeHoldings() {
    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 50_000, FRONT_END_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF607", 50_000, VOLUME_SALES_CHARGE);
    addHoldingAndRSalesCharge(dataFromFds, "RBF608", 50_000, FORMULA_ONE);

    final var sut = new SalesChargeCalculation(dataFromFds);

    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("RBF606", BigDecimal.valueOf(0.3333333333));
    final var rbf607 = new SalesChargeResult.SalesChargeHoldingEntry("RBF607", BigDecimal.valueOf(0.3333333333));
    final var rbf608 = new SalesChargeResult.SalesChargeHoldingEntry("RBF608", BigDecimal.valueOf(0.3333333333));

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000), Set.of(
        rbf606, rbf607, rbf608));
    final var expected = new SalesChargeResult().setSalesCharges(Map.of(
        SalesCharge.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesCharge.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesCharge.DEFERRED_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO));

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculate_checkResultWhenOneSalesChargeHaveOneHoldings() {
    // SETUP
    final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds = new HashMap<>();
    addHoldingAndRSalesCharge(dataFromFds, "RBF606", 150_000, FRONT_END_CHARGE);

    final var sut = new SalesChargeCalculation(dataFromFds);
    final var rbf606 = new SalesChargeResult.SalesChargeHoldingEntry("RBF606", BigDecimal.valueOf(1));

    final var s1 = new SalesChargeResult.SalesChargeEntry(BigDecimal.valueOf(1), BigDecimal.valueOf(150_000), Set.of(
        rbf606));
    final var expected = new SalesChargeResult().setSalesCharges(Map.of(
        SalesCharge.NO_LOAD_INITIAL_SALES_CHARGE, s1,
        SalesCharge.LOW_LOAD_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO,
        SalesCharge.DEFERRED_SALES_CHARGE, DEFAULT_SALES_CHARGE_DTO));

    // ACT
    final var actual = sut.calculate();

    // VERIFY
    assertEquals(expected, actual);
  }

  private void addHoldingAndRSalesCharge(final Map<Holding, com.fintex.ce.domain.model.SalesCharge> dataFromFds,
      final String fundServCode,
      final int value,
      final SalesChargeType frontEndCharge) {
    final FundSeriesHolding holding = new FundSeriesHolding();
    holding.setFundServCode(fundServCode);
    holding.setValue(BigDecimal.valueOf(value));

    dataFromFds.put(holding, new com.fintex.ce.domain.model.SalesCharge(frontEndCharge.name(), null, null, null, null));
  }

}