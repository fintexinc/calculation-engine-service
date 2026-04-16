package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsIntervalResult;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.domain.result.distribution.DistributionRangeResult;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.util.BigDecimalConstants;
import com.fintex.ce.util.DecimalUtils;

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
    final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var reqDTO = new DistributionOfReturnsCommand();

    sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);

    doCallRealMethod().when(sut).calculate(any());
    sut.calculate(reqDTO);

    verify(sut, times(2)).calculateDistributionOfReturnsFor(returns, reqDTO);
  }

  @Test
  void shouldCalculate_whenExpectNullWhenAnnualReturnsIsNull() {
    final var returns = getPortfolioTotalReturns();
    final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var reqDTO = new DistributionOfReturnsCommand();

    sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    final DistributionOfReturnsIntervalResult calculatedMonthlyReturns = mock(
        DistributionOfReturnsIntervalResult.class);
    when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
    when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);

    doCallRealMethod().when(sut).calculate(any());
    sut.calculate(reqDTO);

    verify(sut).initializeResponseDTO(calculatedMonthlyReturns, null, returns);
  }

  @Test
  void shouldReturnInitializedResponse_whenAnnualReturnsAreNull() {
    final var returns = getPortfolioTotalReturns();
    final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var reqDTO = new DistributionOfReturnsCommand();

    sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    final DistributionOfReturnsIntervalResult calculatedMonthlyReturns = mock(
        DistributionOfReturnsIntervalResult.class);
    when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
    when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);
    final DistributionOfReturnsResult expected = mock(DistributionOfReturnsResult.class);
    when(sut.initializeResponseDTO(calculatedMonthlyReturns, null, returns)).thenReturn(expected);

    doCallRealMethod().when(sut).calculate(any());
    final DistributionOfReturnsResult actual = sut.calculate(reqDTO);

    assertEquals(expected, actual);
  }

  @Test
  void shouldInitializeResponseWithMonthlyAndYearlyDistributions_whenExecutingCalculate() {
    final var returns = getPortfolioTotalReturns();
    final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
    final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
    final var reqDTO = new DistributionOfReturnsCommand();

    final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResult();
    final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResult();

    sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

    when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);
    when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(new DistributionOfReturnsIntervalResult());

    doCallRealMethod().when(sut).calculate(any());
    sut.calculate(reqDTO);

    verify(sut).initializeResponseDTO(calculatedMonthlyReturns, calculatedAnnualReturns, returns);
  }

  @Test
  void shouldReadMinAndMaxValues_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var sut = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var reqDTO = mock(DistributionOfReturnsCommand.class);

      when(reqDTO.getCustomNumberOfBins()).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
      sut.calculateDistributionOfReturnsFor(returns, reqDTO);

      mockedDecimalUtils.verify(() -> DecimalUtils.getMinValue(returns));
      mockedDecimalUtils.verify(() -> DecimalUtils.getMaxValue(returns));
    }
  }

  @Test
  void shouldCalculateNumberOfBins_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var sut = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var reqDTO = mock(DistributionOfReturnsCommand.class);

      when(reqDTO.getCustomNumberOfBins()).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
      sut.calculateDistributionOfReturnsFor(returns, reqDTO);

      verify(sut).calculateNumberOfBins(returns, 10);
    }
  }

  @Test
  void shouldCalculateBinWidthIncrements_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var sut = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var reqDTO = mock(DistributionOfReturnsCommand.class);

      when(reqDTO.getCustomNumberOfBins()).thenReturn(10);
      when(sut.calculateNumberOfBins(any(), anyInt())).thenReturn(10);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
      sut.calculateDistributionOfReturnsFor(returns, reqDTO);

      verify(sut).calculateBinWidthIncrements(ONE, TEN_THOUSAND, 10);
    }
  }

  @Test
  void shouldBuildIntervalResult_whenCalculatingDistributionOfReturns() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var sut = mock(DistributionOfReturnsCalculation.class);
      final var returns = getPortfolioTotalReturns();
      final var reqDTO = mock(DistributionOfReturnsCommand.class);
      final var expected = new DistributionOfReturnsIntervalResult(ONE, TEN_THOUSAND, 10, TWELVE, List.of());

      when(reqDTO.getCustomNumberOfBins()).thenReturn(10);
      when(sut.calculateNumberOfBins(any(), anyInt())).thenReturn(10);
      when(sut.calculateBinWidthIncrements(any(), any(), anyInt())).thenReturn(TWELVE);

      mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
      mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

      doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
      final DistributionOfReturnsIntervalResult actual = sut.calculateDistributionOfReturnsFor(returns, reqDTO);

      assertEquals(expected.getDistributionBin(), actual.getDistributionBin());
    }
  }

  @Test
  void shouldUseSquareRootAndFloorScale_whenCustomBinsAreNotProvided() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var sut = mock(DistributionOfReturnsCalculation.class);
      final var portfolioTotalReturns = getPortfolioTotalReturns();

      mockedDecimalUtils.when(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4))).thenReturn(TWO);
      mockedDecimalUtils.when(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR)).thenReturn(TWO);

      doCallRealMethod().when(sut).calculateNumberOfBins(portfolioTotalReturns, null);
      sut.calculateNumberOfBins(portfolioTotalReturns, null);

      mockedDecimalUtils.verify(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4)));
      mockedDecimalUtils.verify(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR));
    }
  }

  @Test
  void shouldReturnCustomNumberOfBins_whenCustomBinsProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var portfolioTotalReturns = getPortfolioTotalReturns();
    final Integer expected = 10;

    doCallRealMethod().when(sut).calculateNumberOfBins(any(), anyInt());
    final Integer actual = sut.calculateNumberOfBins(portfolioTotalReturns, 10);

    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateDefaultNumberOfBins_whenCustomBinsAreNull() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = getPortfolioTotalReturns();

    doCallRealMethod().when(sut).calculateNumberOfBins(portfolioTotalReturns, null);
    final Integer actual = sut.calculateNumberOfBins(portfolioTotalReturns, null);

    assertEquals(2, actual);
  }

  @Test
  void shouldCalculateBinWidthIncrement_whenMinMaxAndBinCountAreProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var MIN = new BigDecimal("5");
    final var MAX = new BigDecimal("20");
    final var numberOfBins = 5;

    doCallRealMethod().when(sut).calculateBinWidthIncrements(MIN, MAX, numberOfBins);
    final BigDecimal actual = sut.calculateBinWidthIncrements(MIN, MAX, numberOfBins);

    final var expected = new BigDecimal("3.000000000000000");
    assertEquals(expected, actual);
  }

  @Test
  void shouldReturnDistributionRanges_whenCalculatingDistribution() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);
    when(sut.initializeDistributionRangeDTO(anyInt(), any(), anyLong())).thenReturn(new DistributionRangeResult(5, TEN,
        2L));

    doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    final List<DistributionRangeResult> actual = sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins,
        returnsBinWidthIncrements);

    final var expected = List.of(new DistributionRangeResult(5, TEN, 2L));
    assertEquals(expected.get(0).getRange(), actual.get(0).getRange());
  }

  @Test
  void shouldInitializeDistributionRangeDTO_whenCalculatingDistribution() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

    verify(sut).initializeDistributionRangeDTO(1, TEN, 10L);
  }

  @Test
  void shouldCalculateFrequencyOfReturns_whenCalculatingDistribution() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

    verify(sut).calculateFrequencyOfReturns(eq(returns), eq(returnsMIN), eq(TEN), anyList());
  }

  @Test
  void shouldCalculateBinInterval_whenCalculatingDistribution() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = new BigDecimal("5");
    final var numberOfBins = 1L;
    final var returnsBinWidthIncrements = TWELVE;

    when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
    when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

    doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
    sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins,
        returnsBinWidthIncrements);

    verify(sut).calculateBinInterval(returnsMIN, 1, returnsBinWidthIncrements);
  }

  @Test
  void shouldCalculateBinIntervalValue_whenInputsAreProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returnsMIN = new BigDecimal("5");
    final var binIndex = 2;
    final var returnsBinWidthIncrements = new BigDecimal("20");

    doCallRealMethod().when(sut).calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);
    final BigDecimal actual = sut.calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);

    final var expected = new BigDecimal("45.0000000000");
    assertEquals(expected, actual);
  }

  @Test
  void shouldUseNotEqualsMinBranch_whenBinIntervalDiffersFromMin() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = ONE;
    final var binInterval = TWO;
    final List<DistributionRangeResult> rangeResDTOS = List.of();

    doCallRealMethod().when(sut).calculateFrequencyOfReturns(any(), any(), any(), anyList());
    sut.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDTOS);

    verify(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);
  }

  @Test
  void shouldUseEqualsMinBranch_whenBinIntervalEqualsMin() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var returnsMIN = TWO;
    final var binInterval = TWO;
    final List<DistributionRangeResult> rangeResDTOS = List.of();

    doCallRealMethod().when(sut).calculateFrequencyOfReturns(any(), any(), any(), anyList());
    sut.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDTOS);

    verify(sut).calculateFrequencyIfBinIntervalEqualsMin(returns, returnsMIN);
  }

  @Test
  void shouldInitializeDistributionRangeDTO_whenInputsAreProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var binIndex = 4;
    final var binInterval = TWO;
    final long frequencyOfReturns = 10L;

    doCallRealMethod().when(sut).initializeDistributionRangeDTO(anyInt(), any(), anyLong());
    final DistributionRangeResult actual = sut.initializeDistributionRangeDTO(binIndex, binInterval,
        frequencyOfReturns);

    final var expected = new DistributionRangeResult(binIndex, binInterval, frequencyOfReturns);
    assertEquals(expected, actual);
  }

  @Test
  void shouldInitializeResponseDTO_whenMonthlyAndYearlyIntervalsProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResult(ONE, TWO, 10, TWELVE, List.of());
    final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResult();

    doCallRealMethod().when(sut).initializeResponseDTO(any(), any(), any());
    final DistributionOfReturnsResult actual = sut.initializeResponseDTO(calculatedMonthlyReturns,
        calculatedAnnualReturns, returns);

    final var expected = new DistributionOfReturnsResult().setMonthlyReturns(calculatedMonthlyReturns).setYearlyReturns(
        calculatedAnnualReturns);
    assertEquals(expected.getMonthlyReturns().getDistributionIncrement(), actual.getMonthlyReturns()
        .getDistributionIncrement());
  }

  @Test
  void shouldCalculateFrequencyForEqualsMin_whenBinIntervalEqualsMin() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var portfolioTotalReturns = getPortfolioTotalReturns();
    final var returnsMIN = HUNDRED;

    doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);
    final long actual = sut.calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);

    assertEquals(3, actual);
  }

  @Test
  void shouldDelegateToEqualsMinMethod_whenNoPreviousRangeExists() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var binInterval = HUNDRED;
    final List<DistributionRangeResult> rangeResDTOS = List.of();

    doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
    sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);

    verify(sut).calculateFrequencyIfBinIntervalEqualsMin(returns, binInterval);
  }

  @Test
  void shouldDelegateToThreeArgOverload_whenPreviousRangeExists() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var binInterval = HUNDRED;
    final var previousBin = mock(DistributionRangeResult.class);
    final var rangeOfPreviousBin = TEN;
    final List<DistributionRangeResult> rangeResDTOS = List.of(mock(DistributionRangeResult.class), previousBin);

    when(previousBin.getRange()).thenReturn(rangeOfPreviousBin);

    doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
    sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);

    verify(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
  }

  @Test
  void shouldCalculateFrequencyForOpenInterval_whenPreviousRangeIsProvided() {
    final var sut = mock(DistributionOfReturnsCalculation.class);
    final var returns = getPortfolioTotalReturns();
    final var rangeOfPreviousBin = TWO;
    final var binInterval = HUNDRED;

    doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
    final long actual = sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);

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