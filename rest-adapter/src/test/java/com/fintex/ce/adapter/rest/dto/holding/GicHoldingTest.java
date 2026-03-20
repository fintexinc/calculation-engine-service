package com.fintex.ce.adapter.rest.dto.holding;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.holding.GicHolding;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.fintex.ce.domain.model.enumeration.Currency.CAD;
import static com.fintex.ce.domain.model.enumeration.Currency.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GicHoldingTest {

  private static final BigDecimal LESS_THAN_YEAR = BigDecimal.valueOf(360);
  private static final BigDecimal GREATER_THAN_YEAR = BigDecimal.valueOf(365);

  @Test
  void getCurrency_ifEmptyGetCAD() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);

    // ACT
    final Currency actual = sut.getCurrency();

    // VERIFY
    assertEquals(CAD, actual);
  }

  @Test
  void getCurrency_getEnteredCurrency() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setCurrency(USD);

    // ACT
    final Currency actual = sut.getCurrency();

    // VERIFY
    assertEquals(USD, actual);
  }

  @Test
  void getInceptionDate_ifEmptyGetDefaultStartDate() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);

    // ACT
    final LocalDate actual = sut.getInvestmentDate();

    // VERIFY
    assertEquals(LocalDate.of(1954, 1, 31), actual);
  }

  @Test
  void getCompoundingFrequency_ifEmptyGetAnnual() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);

    // ACT
    final InterestFreq actual = sut.getInterestFreq();

    // VERIFY
    assertEquals(InterestFreq.ANNUAL, actual);
  }

  @Test
  void getCompoundingFrequency_getEnteredFrequencyAnnual() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setInterestFreq(InterestFreq.SEMI_ANNUAL);

    // ACT
    final InterestFreq actual = sut.getInterestFreq();

    // VERIFY
    assertEquals(InterestFreq.SEMI_ANNUAL, actual);
  }

  @Test
  void getAssetAllocation_ifLessThanOneYearThanCash() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setTerm(LESS_THAN_YEAR);

    // ACT
    final AssetAllocationRegion actual = sut.getAssetAllocation();

    // VERIFY
    assertEquals(AssetAllocationRegion.CASH, actual);
  }

  @Test
  void getAssetAllocation_ifMoreThanOneYearThanFixedIncome() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setTerm(GREATER_THAN_YEAR);

    // ACT
    final AssetAllocationRegion actual = sut.getAssetAllocation();

    // VERIFY
    assertEquals(AssetAllocationRegion.FIXED_INCOME, actual);
  }

  @Test
  void isLessThanOneYear_lessThanOneYear() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setTerm(LESS_THAN_YEAR);

    // ACT
    final boolean result = sut.isLessThanOneYearOld();

    // VERIFY
    assertTrue(result);
  }

  @Test
  void isLessThanOneYear_OneYear() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setTerm(GREATER_THAN_YEAR);

    // ACT
    final boolean result = sut.isLessThanOneYearOld();

    // VERIFY
    assertFalse(result);
  }

  @Test
  void isLessThanOneYear_moreThanOneYear() {
    // SETUP
    final GicHolding sut = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    sut.setTerm(GREATER_THAN_YEAR);

    // ACT
    final boolean result = sut.isLessThanOneYearOld();

    // VERIFY
    assertFalse(result);
  }

  @Test
  void getInterestRate_checkResult() {
    // SETUP
    final GicHolding sut = new GicHolding();
    sut.setClientIntRate(BigDecimal.valueOf(1));

    // ACT
    final var result = sut.getClientIntRate();

    // VERIFY
    assertEquals(BigDecimal.valueOf(1), result);
  }

}
