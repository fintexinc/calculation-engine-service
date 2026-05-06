package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsIntervalResult;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.domain.result.distribution.DistributionRangeResult;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DistributionOfReturnsCalculationTest {

  @Test
  void shouldCalculateDistributionForMonthlyAndYearlyReturns_whenExecutingCalculate() {
    final var returns = getPortfolioTotalReturns();
    final var calculation = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var command = new DistributionOfReturnsCommand();

    calculation.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    when(calculation.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);

    doCallRealMethod().when(calculation).calculate(any());
    calculation.calculate(command);

    verify(calculation, times(2)).calculateDistributionOfReturnsFor(returns, command);
  }

  @Test
  void shouldCalculate_whenExpectNullWhenAnnualReturnsIsNull() {
    final var returns = getPortfolioTotalReturns();
    final var calculation = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var command = new DistributionOfReturnsCommand();

    calculation.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    final DistributionOfReturnsIntervalResult calculatedMonthlyReturns = mock(
        DistributionOfReturnsIntervalResult.class);
    when(calculation.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
    when(calculation.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);

    doCallRealMethod().when(calculation).calculate(any());
    calculation.calculate(command);

    verify(calculation).initializeResult(calculatedMonthlyReturns, null, returns);
  }

  @Test
  void shouldReturnInitializedResponse_whenAnnualReturnsAreNull() {
    final var returns = getPortfolioTotalReturns();
    final var calculation = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var command = new DistributionOfReturnsCommand();

    calculation.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    final DistributionOfReturnsIntervalResult calculatedMonthlyReturns = mock(
        DistributionOfReturnsIntervalResult.class);
    when(calculation.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
    when(calculation.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);
    final DistributionOfReturnsResult expected = mock(DistributionOfReturnsResult.class);
    when(calculation.initializeResult(calculatedMonthlyReturns, null, returns)).thenReturn(expected);

    doCallRealMethod().when(calculation).calculate(any());
    final DistributionOfReturnsResult actual = calculation.calculate(command);

    assertEquals(expected, actual);
  }

  @Test
  void shouldInitializeResponseWithMonthlyAndYearlyDistributions_whenExecutingCalculate() {
    final var returns = getPortfolioTotalReturns();
    final var calculation = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var command = new DistributionOfReturnsCommand();

    final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResult(null, null, 0, null, null);
    final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResult(null, null, 0, null, null);

    calculation.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    when(calculation.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);
    when(calculation.calculateDistributionOfReturnsFor(any(), any())).thenReturn(
        new DistributionOfReturnsIntervalResult(null, null, 0, null, null));

    doCallRealMethod().when(calculation).calculate(any());
    calculation.calculate(command);

    verify(calculation).initializeResult(calculatedMonthlyReturns, calculatedAnnualReturns, returns);
  }

  @Test
  void shouldReadMinAndMaxValues_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var calculation = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var command = mock(DistributionOfReturnsCommand.class);

      when(command.getCustomNumberOfBins()).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(calculation).calculateDistributionOfReturnsFor(any(), any());
      calculation.calculateDistributionOfReturnsFor(returns, command);

      mockedDecimalUtils.verify(() -> DecimalUtils.getMinValue(returns));
      mockedDecimalUtils.verify(() -> DecimalUtils.getMaxValue(returns));
    }
  }

  @Test
  void shouldCalculateNumberOfBins_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var calculation = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var command = mock(DistributionOfReturnsCommand.class);

      when(command.getCustomNumberOfBins()).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(calculation).calculateDistributionOfReturnsFor(any(), any());
      calculation.calculateDistributionOfReturnsFor(returns, command);

      verify(calculation).calculateNumberOfBins(returns, 10);
    }
  }

  @Test
  void shouldCalculateBinWidthIncrements_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var calculation = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var command = mock(DistributionOfReturnsCommand.class);

      when(command.getCustomNumberOfBins()).thenReturn(10);
      when(calculation.calculateNumberOfBins(any(), anyInt())).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(calculation).calculateDistributionOfReturnsFor(any(), any());
      calculation.calculateDistributionOfReturnsFor(returns, command);

      verify(calculation).calculateBinWidthIncrements(ONE, TEN_THOUSAND, 10);
    }
  }

  @Test
  void shouldBuildIntervalResult_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var calculation = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var command = mock(DistributionOfReturnsCommand.class);
      final var expected = new DistributionOfReturnsIntervalResult(ONE, TEN_THOUSAND, 10, TWELVE, List.of());

      when(command.getCustomNumberOfBins()).thenReturn(10);
      when(calculation.calculateNumberOfBins(any(), anyInt())).thenReturn(10);
      when(calculation.calculateBinWidthIncrements(any(), any(), anyInt())).thenReturn(TWELVE);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(calculation).calculateDistributionOfReturnsFor(any(), any());
      final DistributionOfReturnsIntervalResult actual = calculation.calculateDistributionOfReturnsFor(returns, command);

      assertEquals(expected.distributionBin(), actual.distributionBin());
    }
  }

  @Test
  void shouldUseSquareRootAndFloorScale_whenCustomBinsAreNotProvided() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var calculation = mock(DistributionOfReturnsCalculation.class);
      final var portfolioTotalReturns = getPortfolioTotalReturns();

      mockedDecimalUtils.when(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4))).thenReturn(TWO);
      mockedDecimalUtils.when(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR)).thenReturn(TWO);

      doCallRealMethod().when(calculation).calculateNumberOfBins(portfolioTotalReturns, null);
      calculation.calculateNumberOfBins(portfolioTotalReturns, null);

      mockedDecimalUtils.verify(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4)));
      mockedDecimalUtils.verify(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR));
    }
  }

  @Test
  void shouldReturnCustomNumberOfBins_whenCustomBinsProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var portfolioTotalReturns = getPortfolioTotalReturns();
    final Integer expected = 10;

    doCallRealMethod().when(calculation).calculateNumberOfBins(any(), anyInt());
    final Integer actual = calculation.calculateNumberOfBins(portfolioTotalReturns, 10);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDefaultNumberOfBins_whenCustomBinsAreNull() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = getPortfolioTotalReturns();

    doCallRealMethod().when(calculation).calculateNumberOfBins(portfolioTotalReturns, null);
    final Integer actual = calculation.calculateNumberOfBins(portfolioTotalReturns, null);

    assertEquals(2, actual);
  }

  @Test
  void shouldCalculateBinWidthIncrement_whenMinMaxAndBinCountAreProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var MIN = new BigDecimal("5");
    final var MAX = new BigDecimal("20");
    final var numberOfBins = 5;

    doCallRealMethod().when(calculation).calculateBinWidthIncrements(MIN, MAX, numberOfBins);
    final BigDecimal actual = calculation.calculateBinWidthIncrements(MIN, MAX, numberOfBins);

    final var expected = new BigDecimal("3.000000000000000");
    assertEquals(expected, actual);
  }

  @Test
  void shouldReturnDistributionRanges_whenCalculatingDistribution() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(calculation.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(calculation.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);
    when(calculation.initializeDistributionRange(anyInt(), any(), anyLong())).thenReturn(new DistributionRangeResult(5, TEN,
        2L));

    doCallRealMethod().when(calculation).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    final List<DistributionRangeResult> actual = calculation.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins,
        returnsBinWidthIncrements);

    final var expected = List.of(new DistributionRangeResult(5, TEN, 2L));
    assertEquals(expected.get(0).range(), actual.get(0).range());
  }

  @Test
  void shouldInitializeDistributionRangeDto_whenCalculatingDistribution() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(calculation.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(calculation.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(calculation).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    calculation.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

    verify(calculation).initializeDistributionRange(1, TEN, 10L);
  }

  @Test
  void shouldCalculateFrequencyOfReturns_whenCalculatingDistribution() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(calculation.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(calculation.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(calculation).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    calculation.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

    verify(calculation).calculateFrequencyOfReturns(eq(returns), eq(returnsMIN), eq(TEN), anyList());
  }

  @Test
  void shouldCalculateBinInterval_whenCalculatingDistribution() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(calculation.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(calculation.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(calculation).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    calculation.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins,
        returnsBinWidthIncrements);

    verify(calculation).calculateBinInterval(returnsMIN, 1, returnsBinWidthIncrements);
  }

  @Test
  void shouldCalculateBinIntervalValue_whenInputsAreProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returnsMIN = new BigDecimal("5");
    final var binIndex = 2;
    final var returnsBinWidthIncrements = new BigDecimal("20");

    doCallRealMethod().when(calculation).calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);
    final BigDecimal actual = calculation.calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);

    final var expected = new BigDecimal("45.0000000000");
    assertEquals(expected, actual);
  }

  @Test
  void shouldUseNotEqualsMinBranch_whenBinIntervalDiffersFromMin() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = ONE;
    final var binInterval = TWO;
    final List<DistributionRangeResult> rangeResDtoS = List.of();

    doCallRealMethod().when(calculation).calculateFrequencyOfReturns(any(), any(), any(), anyList());
    calculation.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDtoS);

    verify(calculation).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDtoS);
  }

  @Test
  void shouldUseEqualsMinBranch_whenBinIntervalEqualsMin() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = TWO;
    final var binInterval = TWO;
    final List<DistributionRangeResult> rangeResDtoS = List.of();

    doCallRealMethod().when(calculation).calculateFrequencyOfReturns(any(), any(), any(), anyList());
    calculation.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDtoS);

    verify(calculation).calculateFrequencyIfBinIntervalEqualsMin(returns, returnsMIN);
  }

  @Test
  void shouldInitializeDistributionRangeDto_whenInputsAreProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var binIndex = 4;
    final var binInterval = TWO;
    final long frequencyOfReturns = 10L;

    doCallRealMethod().when(calculation).initializeDistributionRange(anyInt(), any(), anyLong());
    final DistributionRangeResult actual = calculation.initializeDistributionRange(binIndex, binInterval,
        frequencyOfReturns);

    final var expected = new DistributionRangeResult(binIndex, binInterval, frequencyOfReturns);
    assertEquals(expected, actual);
  }

  @Test
  void shouldInitializeResponseDto_whenMonthlyAndYearlyIntervalsProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResult(ONE, TWO, 10, TWELVE, List.of());
    final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResult(null, null, 0, null, null);

    doCallRealMethod().when(calculation).initializeResult(any(), any(), any());
    final DistributionOfReturnsResult actual = calculation.initializeResult(calculatedMonthlyReturns,
        calculatedAnnualReturns, returns);

    final var expected = DistributionOfReturnsResult.builder()
        .monthlyReturns(calculatedMonthlyReturns)
        .yearlyReturns(calculatedAnnualReturns)
        .build();
    assertEquals(expected.getMonthlyReturns().distributionIncrement(), actual.getMonthlyReturns()
        .distributionIncrement());
  }

  @Test
  void shouldCalculateFrequencyForEqualsMin_whenBinIntervalEqualsMin() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var portfolioTotalReturns = getPortfolioTotalReturns();
    final var returnsMIN = HUNDRED;

    doCallRealMethod().when(calculation).calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);
    final long actual = calculation.calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);

    assertEquals(3, actual);
  }

  @Test
  void shouldDelegateToEqualsMinMethod_whenNoPreviousRangeExists() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var binInterval = HUNDRED;
    final List<DistributionRangeResult> rangeResDtoS = List.of();

    doCallRealMethod().when(calculation).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
    calculation.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDtoS);

    verify(calculation).calculateFrequencyIfBinIntervalEqualsMin(returns, binInterval);
  }

  @Test
  void shouldDelegateToThreeArgOverload_whenPreviousRangeExists() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var binInterval = HUNDRED;
    final var previousBin = mock(DistributionRangeResult.class);
    final var rangeOfPreviousBin = TEN;
    final List<DistributionRangeResult> rangeResDtoS = List.of(mock(DistributionRangeResult.class), previousBin);

    when(previousBin.range()).thenReturn(rangeOfPreviousBin);

    doCallRealMethod().when(calculation).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
    calculation.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDtoS);

    verify(calculation).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
  }

  @Test
  void shouldCalculateFrequencyForOpenInterval_whenPreviousRangeIsProvided() {
    final var calculation = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var rangeOfPreviousBin = TWO;
    final var binInterval = HUNDRED;

    doCallRealMethod().when(calculation).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
    final long actual = calculation.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);

    assertEquals(2, actual);
  }

  private TreeMap<LocalDate, BigDecimal> getPortfolioTotalReturns() {
    final var portfolioTotalReturns = new TreeMap<LocalDate, BigDecimal>();
    portfolioTotalReturns.put(LocalDate.now().minusMonths(5), BigDecimalConstants.HUNDRED);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(6), BigDecimalConstants.TEN_THOUSAND);
    portfolioTotalReturns.put(LocalDate.now().minusMonths(1), BigDecimal.ZERO);
    return portfolioTotalReturns;
  }
}