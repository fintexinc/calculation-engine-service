package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.rangeWithLastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GicMonthlyReturnsGeneratorTest {

  static final BigDecimal RETURN_OF_2_PERCENT_ANNUAL = BigDecimal.valueOf(0.0016515813019202);
  static final BigDecimal RETURN_OF_10_PERCENT_ANNUAL = BigDecimal.valueOf(0.0079741404289038);
  static final BigDecimal RETURN_OF_5_PERCENT_SEMI_ANNUAL = BigDecimal.valueOf(0.0041239154651442);
  static final BigDecimal RETURN_OF_5_PERCENT_QUARTERLY = BigDecimal.valueOf(0.0041494251232543);
  static final BigDecimal RETURN_OF_5_PERCENT_MONTHLY = BigDecimal.valueOf(0.004166666666667);
  static final BigDecimal RETURN_OF_5_PERCENT_BI_MONTHLY = BigDecimal.valueOf(0.0041580220928044);
  static final BigDecimal RETURN_OF_5_PERCENT_WEEKLY = BigDecimal.valueOf(0.0041733490124376);
  static final BigDecimal RETURN_OF_5_PERCENT_BI_WEEKLY = BigDecimal.valueOf(0.0041713413111404);
  static final BigDecimal RETURN_OF_5_PERCENT_DAILY = BigDecimal.valueOf(0.0041750727376082);

  @Test
  void shouldGenerateGicMonthlyReturns_when2PercentAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(2))
        .investmentDate(LocalDate.of(2020, 5, 1))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_2_PERCENT_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when10PercentAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(10))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_10_PERCENT_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentSemiAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.SEMI_ANNUAL)
        .investmentDate(LocalDate.of(2005, 2, 1))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_SEMI_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentQuarterlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.QUARTERLY)
        .investmentDate(LocalDate.of(2018, 9, 28))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_QUARTERLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentMonthlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.MONTHLY)
        .investmentDate(LocalDate.of(2000, 3, 15))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_MONTHLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentBiMonthlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.BI_MONTHLY)
        .investmentDate(LocalDate.of(2020, 1, 1))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_BI_MONTHLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentWeeklyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.WEEKLY)
        .investmentDate(LocalDate.of(2019, 7, 15))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_WEEKLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentBiWeeklyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.BI_WEEKLY)
        .investmentDate(LocalDate.of(2020, 5, 31))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_BI_WEEKLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentDailyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = GicHolding.builder()
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(5))
        .interestFreq(InterestFreq.DAILY)
        .investmentDate(LocalDate.of(2019, 6, 12))
        .build();

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_DAILY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  private HashMap<PortfolioHolding, HoldingMonthlyReturns> getExpected(final GicHolding gicHolding,
      final BigDecimal gicReturnValue) {
    final HashMap<PortfolioHolding, HoldingMonthlyReturns> result = new HashMap<>();

    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    rangeWithLastDayOfMonth(gicHolding.getInvestmentDate(), LocalDate.now())
        .forEach(localDate -> returns.put(localDate, gicReturnValue));

    final HoldingMonthlyReturns rMonthlyReturns = new HoldingMonthlyReturns();
    rMonthlyReturns.setHoldingType(FinancialInstrumentType.GIC);
    rMonthlyReturns.setCurrency(gicHolding.getCurrency().name());
    rMonthlyReturns.setReturns(returns);

    result.put(gicHolding, rMonthlyReturns);
    return result;
  }

}
