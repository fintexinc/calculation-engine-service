package com.fintex.ce.adapter.rest.dto.holding;

import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GicHoldingTest {

  private static final BigDecimal LESS_THAN_YEAR = BigDecimal.valueOf(360);
  private static final BigDecimal GREATER_THAN_YEAR = BigDecimal.valueOf(365);

  @Test
  void getCurrency_ifEmptyGetCAD() {
    final GicHolding sut = GicHolding.builder().value(BigDecimal.ONE).holdingType(FinancialInstrumentType.GIC).build();

    final CurrencyType actual = sut.getCurrency();

    assertEquals(CAD, actual);
  }

  @Test
  void getCurrency_getEnteredCurrency() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .currency(USD)
        .build();

    final CurrencyType actual = sut.getCurrency();

    assertEquals(USD, actual);
  }

  @Test
  void getInceptionDate_ifEmptyGetDefaultStartDate() {
    final GicHolding sut = GicHolding.builder().value(BigDecimal.ONE).holdingType(FinancialInstrumentType.GIC).build();

    final LocalDate actual = sut.getInvestmentDate();

    assertEquals(LocalDate.of(1954, 1, 31), actual);
  }

  @Test
  void getCompoundingFrequency_ifEmptyGetAnnual() {
    final GicHolding sut = GicHolding.builder().value(BigDecimal.ONE).holdingType(FinancialInstrumentType.GIC).build();

    final InterestFreq actual = sut.getInterestFreq();

    assertEquals(InterestFreq.ANNUAL, actual);
  }

  @Test
  void getCompoundingFrequency_getEnteredFrequencyAnnual() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .interestFreq(InterestFreq.SEMI_ANNUAL)
        .build();

    final InterestFreq actual = sut.getInterestFreq();

    assertEquals(InterestFreq.SEMI_ANNUAL, actual);
  }

  @Test
  void getAssetAllocation_ifLessThanOneYearThanCash() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .term(LESS_THAN_YEAR)
        .build();

    final AssetAllocationRegion actual = sut.getAssetAllocation();

    assertEquals(AssetAllocationRegion.CASH, actual);
  }

  @Test
  void getAssetAllocation_ifMoreThanOneYearThanFixedIncome() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .term(GREATER_THAN_YEAR)
        .build();

    final AssetAllocationRegion actual = sut.getAssetAllocation();

    assertEquals(AssetAllocationRegion.FIXED_INCOME, actual);
  }

  @Test
  void isLessThanOneYear_lessThanOneYear() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .term(LESS_THAN_YEAR)
        .build();

    final boolean result = sut.isLessThanOneYearOld();

    assertTrue(result);
  }

  @Test
  void isLessThanOneYear_OneYear() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .term(GREATER_THAN_YEAR)
        .build();

    final boolean result = sut.isLessThanOneYearOld();

    assertFalse(result);
  }

  @Test
  void isLessThanOneYear_moreThanOneYear() {
    final GicHolding sut = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .term(GREATER_THAN_YEAR)
        .build();

    final boolean result = sut.isLessThanOneYearOld();

    assertFalse(result);
  }

  @Test
  void getInterestRate_checkResult() {
    final GicHolding sut = GicHolding.builder()
        .clientIntRate(BigDecimal.valueOf(1))
        .build();

    final var result = sut.getClientIntRate();

    assertEquals(BigDecimal.valueOf(1), result);
  }

}
