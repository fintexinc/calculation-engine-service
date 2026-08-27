package ca.tangerine.pce.application.returns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ca.tangerine.pce.util.DateTimeUtils.rangeWithLastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.enumeration.InterestFreq;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.util.PortfolioHoldingBuildHelper;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

class GicMonthlyReturnsGeneratorTest {

  // Constants are in percent form (e.g. 0.165 means 0.165% monthly), matching the generator's output contract
  // post-normalization. Each value is exactly 100× the equivalent decimal-form monthly return. The trailing zeros
  // are intentional — BigDecimal.multiply preserves scale, so multiplying a scale-16 decimal value by 100 yields
  // a scale-16 percent value, and BigDecimal equality (used by the map comparison) is scale-sensitive.
  static final BigDecimal RETURN_OF_2_PERCENT_ANNUAL = new BigDecimal("0.1651581301920200");
  static final BigDecimal RETURN_OF_10_PERCENT_ANNUAL = new BigDecimal("0.7974140428903800");
  static final BigDecimal RETURN_OF_5_PERCENT_SEMI_ANNUAL = new BigDecimal("0.4123915465144200");
  static final BigDecimal RETURN_OF_5_PERCENT_QUARTERLY = new BigDecimal("0.4149425123254300");
  static final BigDecimal RETURN_OF_5_PERCENT_MONTHLY = new BigDecimal("0.416666666666700");
  static final BigDecimal RETURN_OF_5_PERCENT_BI_MONTHLY = new BigDecimal("0.4158022092804400");
  static final BigDecimal RETURN_OF_5_PERCENT_WEEKLY = new BigDecimal("0.4173349012437600");
  static final BigDecimal RETURN_OF_5_PERCENT_BI_WEEKLY = new BigDecimal("0.4171341311140400");
  static final BigDecimal RETURN_OF_5_PERCENT_DAILY = new BigDecimal("0.4175072737608200");

  @Test
  void shouldGenerateGicMonthlyReturns_when2PercentAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(2), null,
        LocalDate.of(2020, 5, 1));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_2_PERCENT_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when10PercentAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(10), null, null);

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_10_PERCENT_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentSemiAnnualFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.SEMI_ANNUAL,
        LocalDate.of(2005, 2, 1));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_SEMI_ANNUAL);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentQuarterlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.QUARTERLY,
        LocalDate.of(2018, 9, 28));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_QUARTERLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentMonthlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.MONTHLY,
        LocalDate.of(2000, 3, 15));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_MONTHLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentBiMonthlyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.BI_MONTHLY,
        LocalDate.of(2020, 1, 1));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_BI_MONTHLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentWeeklyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.WEEKLY,
        LocalDate.of(2019, 7, 15));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_WEEKLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentBiWeeklyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.BI_WEEKLY,
        LocalDate.of(2020, 5, 31));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_BI_WEEKLY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGenerateGicMonthlyReturns_when5PercentDailyFrequency() {
    // SETUP
    final var generator = new MonthlyReturnsGenerator();
    final var gicHolding = gic(BigDecimal.valueOf(5), InterestFreq.DAILY,
        LocalDate.of(2019, 6, 12));

    final HashMap<PortfolioHolding, HoldingMonthlyReturns> expected = getExpected(gicHolding,
        RETURN_OF_5_PERCENT_DAILY);

    // ACT
    final Map<PortfolioHolding, HoldingMonthlyReturns> actual = generator.generateGicMonthlyReturns(List.of(
        gicHolding));

    // VERIFY
    assertEquals(expected, actual);
  }

  private static GicHolding gic(BigDecimal clientIntRate, InterestFreq interestFreq, LocalDate investmentDate) {
    return PortfolioHoldingBuildHelper.gic(null, null, null, null, clientIntRate, interestFreq, investmentDate);
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
