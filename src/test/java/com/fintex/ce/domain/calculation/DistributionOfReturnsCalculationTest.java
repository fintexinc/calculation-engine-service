package com.fintex.ce.domain.calculation;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.dto.response.distributionofreturns.DistributionOfReturnsIntervalResDTO;
import com.fintex.ce.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.dto.response.distributionofreturns.DistributionRangeResDTO;
import com.fintex.ce.util.DecimalUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.config.constant.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.config.constant.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static org.junit.Assert.assertEquals;
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
    void calculate_verifyCalculateDistributionOfReturnsFor_verifyCalculateDistributionOfReturnsFor() {
        //SETUP
        final var returns = getPortfolioTotalReturns();
        final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
        final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
        final var reqDTO = new DistributionOfReturnsReqDTO();

        sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

        when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);

        doCallRealMethod().when(sut).calculate(any());
        //ACT
        sut.calculate(reqDTO);

        //VERIFY
        verify(sut, times(2)).calculateDistributionOfReturnsFor(returns, reqDTO);
    }

    @Test
    void calculate_expectNullWhenAnnualReturnsIsNull() {
        //SETUP
        final var returns = getPortfolioTotalReturns();
        final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
        final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
        final var reqDTO = new DistributionOfReturnsReqDTO();

        sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

        final DistributionOfReturnsIntervalResDTO calculatedMonthlyReturns = mock(DistributionOfReturnsIntervalResDTO.class);
        when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
        when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);

        doCallRealMethod().when(sut).calculate(any());
        //ACT
        sut.calculate(reqDTO);

        //VERIFY
        verify(sut).initializeResponseDTO(calculatedMonthlyReturns, null, returns);
    }

    @Test
    void calculate_checkResultWhenAnnualReturnsIsNull() {
        //SETUP
        final var returns = getPortfolioTotalReturns();
        final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
        final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
        final var reqDTO = new DistributionOfReturnsReqDTO();

        sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

        final DistributionOfReturnsIntervalResDTO calculatedMonthlyReturns = mock(DistributionOfReturnsIntervalResDTO.class);
        when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(calculatedMonthlyReturns);
        when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(null);
        final DistributionOfReturnsResDTO expected = mock(DistributionOfReturnsResDTO.class);
        when(sut.initializeResponseDTO(calculatedMonthlyReturns, null, returns)).thenReturn(expected);

        doCallRealMethod().when(sut).calculate(any());
        //ACT
        final DistributionOfReturnsResDTO actual = sut.calculate(reqDTO);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculate_verifyCalculateDistributionOfReturnsFor_verifyInitializeResponseDTO() {
        //SETUP
        final var returns = getPortfolioTotalReturns();
        final var sut = mock(DistributionOfReturnsCalculation.class, withSettings().useConstructor(null, returns));
        final var rollingTotalReturnsCalculation = mock(RollingTotalReturnsCalculation.class);
        final var reqDTO = new DistributionOfReturnsReqDTO();

        final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResDTO();
        final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResDTO();

        sut.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;

        when(sut.rollingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(returns);
        when(sut.calculateDistributionOfReturnsFor(any(), any())).thenReturn(new DistributionOfReturnsIntervalResDTO());

        doCallRealMethod().when(sut).calculate(any());
        //ACT
        sut.calculate(reqDTO);

        //VERIFY
        verify(sut).initializeResponseDTO(calculatedMonthlyReturns, calculatedAnnualReturns, returns);
    }

    @Test
    void calculateDistributionOfReturnsFor_verifyGetMinAndMaxValues() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(DistributionOfReturnsCalculation.class);
            final var returns = getPortfolioTotalReturns();
            final var reqDTO = mock(DistributionOfReturnsReqDTO.class);

            when(reqDTO.getCustomNumberOfBins()).thenReturn(10);

            mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
            mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

            doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
            //ACT
            sut.calculateDistributionOfReturnsFor(returns, reqDTO);

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.getMinValue(returns));
            mockedDecimalUtils.verify(() -> DecimalUtils.getMaxValue(returns));
        }
    }

    @Test
    void calculateDistributionOfReturnsFor_verifyCalculateNumberOfBins() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(DistributionOfReturnsCalculation.class);
            final var returns = getPortfolioTotalReturns();
            final var reqDTO = mock(DistributionOfReturnsReqDTO.class);

            when(reqDTO.getCustomNumberOfBins()).thenReturn(10);

            mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
            mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

            doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
            //ACT
            sut.calculateDistributionOfReturnsFor(returns, reqDTO);

            //VERIFY
            verify(sut).calculateNumberOfBins(returns, 10);
        }
    }

    @Test
    void calculateDistributionOfReturnsFor_verifyCalculateBinWidthIncrements() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(DistributionOfReturnsCalculation.class);
            final var returns = getPortfolioTotalReturns();
            final var reqDTO = mock(DistributionOfReturnsReqDTO.class);

            when(reqDTO.getCustomNumberOfBins()).thenReturn(10);
            when(sut.calculateNumberOfBins(any(), anyInt())).thenReturn(10);

            mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
            mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

            doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
            //ACT
            sut.calculateDistributionOfReturnsFor(returns, reqDTO);

            //VERIFY
            verify(sut).calculateBinWidthIncrements(ONE, TEN_THOUSAND, 10);
        }
    }

    @Test
    void calculateDistributionOfReturnsFor_verifyCalculateDistributionOfReturns() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(DistributionOfReturnsCalculation.class);
            final var returns = getPortfolioTotalReturns();
            final var reqDTO = mock(DistributionOfReturnsReqDTO.class);
            final var expected = new DistributionOfReturnsIntervalResDTO(ONE, TEN_THOUSAND, 10, TWELVE, List.of());

            when(reqDTO.getCustomNumberOfBins()).thenReturn(10);
            when(sut.calculateNumberOfBins(any(), anyInt())).thenReturn(10);
            when(sut.calculateBinWidthIncrements(any(), any(), anyInt())).thenReturn(TWELVE);

            mockedDecimalUtils.when(() -> DecimalUtils.getMinValue(returns)).thenReturn(ONE);
            mockedDecimalUtils.when(() -> DecimalUtils.getMaxValue(returns)).thenReturn(TEN_THOUSAND);

            doCallRealMethod().when(sut).calculateDistributionOfReturnsFor(any(), any());
            //ACT
            final DistributionOfReturnsIntervalResDTO actual = sut.calculateDistributionOfReturnsFor(returns, reqDTO);

            //VERIFY
            Assertions.assertEquals(expected.getDistributionBin(), actual.getDistributionBin());
        }
    }

    @Test
    void calculateNumberOfBins_verifySquareRootAndSetInternalScale() {
        try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(DistributionOfReturnsCalculation.class);
            final var portfolioTotalReturns = getPortfolioTotalReturns();

            mockedDecimalUtils.when(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4))).thenReturn(TWO);
            mockedDecimalUtils.when(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR)).thenReturn(TWO);

            doCallRealMethod().when(sut).calculateNumberOfBins(portfolioTotalReturns, null);
            //ACT
            sut.calculateNumberOfBins(portfolioTotalReturns, null);

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.squareRoot(BigDecimal.valueOf(4)));
            mockedDecimalUtils.verify(() -> DecimalUtils.setInternalScale(TWO, RoundingMode.FLOOR));
        }
    }

    @Test
    void calculateNumberOfBins_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var portfolioTotalReturns = getPortfolioTotalReturns();
        final Integer expected = 10;

        doCallRealMethod().when(sut).calculateNumberOfBins(any(), anyInt());
        //ACT
        final Integer actual = sut.calculateNumberOfBins(portfolioTotalReturns, 10);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculateNumberOfBins_checkResult2() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final TreeMap<LocalDate, BigDecimal> portfolioTotalReturns = getPortfolioTotalReturns();

        doCallRealMethod().when(sut).calculateNumberOfBins(portfolioTotalReturns, null);
        //ACT
        final Integer actual = sut.calculateNumberOfBins(portfolioTotalReturns, null);

        //VERIFY
        Assertions.assertEquals(2, actual);
    }

    @Test
    void calculateBinWidthIncrements_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var MIN = new BigDecimal("5");
        final var MAX = new BigDecimal("20");
        final var numberOfBins = 5;

        doCallRealMethod().when(sut).calculateBinWidthIncrements(MIN, MAX, numberOfBins);
        //ACT
        final BigDecimal actual = sut.calculateBinWidthIncrements(MIN, MAX, numberOfBins);

        final var expected = new BigDecimal("3.000000000000000");
        //VERIFY
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void calculateDistributionOfReturns_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = new BigDecimal("5");
        final var numberOfBins = 1L;
        final var returnsBinWidthIncrements = TWELVE;

        when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
        when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);
        when(sut.initializeDistributionRangeDTO(anyInt(), any(), anyLong())).thenReturn(new DistributionRangeResDTO(5, TEN, 2L));

        doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
        //ACT
        final List<DistributionRangeResDTO> actual = sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

        //VERIFY
        final var expected = List.of(new DistributionRangeResDTO(5, TEN, 2L));
        Assertions.assertEquals(expected.get(0).getRange(), actual.get(0).getRange());
    }

    @Test
    void calculateDistributionOfReturns_initializeDistributionRangeDTO() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = new BigDecimal("5");
        final var numberOfBins = 1L;
        final var returnsBinWidthIncrements = TWELVE;

        when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
        when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

        doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
        //ACT
        sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

        //VERIFY
        verify(sut).initializeDistributionRangeDTO(1, TEN, 10L);
    }

    @Test
    void calculateDistributionOfReturns_verifyCalculateFrequencyOfReturns() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = new BigDecimal("5");
        final var numberOfBins = 1L;
        final var returnsBinWidthIncrements = TWELVE;

        when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
        when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

        doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
        //ACT
        sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

        //VERIFY
        verify(sut).calculateFrequencyOfReturns(eq(returns), eq(returnsMIN), eq(TEN), anyList());
    }

    @Test
    void calculateDistributionOfReturns_verifyCalculateBinInterval() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = new BigDecimal("5");
        final var numberOfBins = 1L;
        final var returnsBinWidthIncrements = TWELVE;

        when(sut.calculateBinInterval(any(), anyInt(), any())).thenReturn(TEN);
        when(sut.calculateFrequencyOfReturns(any(), any(), any(), anyList())).thenReturn(10L);

        doCallRealMethod().when(sut).calculateDistributionOfReturns(any(), any(), anyLong(), any());
        //ACT
        final List<DistributionRangeResDTO> actual = sut.calculateDistributionOfReturns(returns, returnsMIN, numberOfBins, returnsBinWidthIncrements);

        final var expected = new BigDecimal("45.0000000000");
        //VERIFY
        verify(sut).calculateBinInterval(returnsMIN, 1, returnsBinWidthIncrements);
    }

    @Test
    void calculateBinInterval_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returnsMIN = new BigDecimal("5");
        final var binIndex = 2;
        final var returnsBinWidthIncrements = new BigDecimal("20");

        doCallRealMethod().when(sut).calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);
        //ACT
        final BigDecimal actual = sut.calculateBinInterval(returnsMIN, binIndex, returnsBinWidthIncrements);

        final var expected = new BigDecimal("45.0000000000");
        //VERIFY
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void calculateFrequencyOfReturns_verifycalculateFrequencyIfBinIntervalEqualsMIN() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = ONE;
        final var binInterval = TWO;
        final List<DistributionRangeResDTO> rangeResDTOS = List.of();

        doCallRealMethod().when(sut).calculateFrequencyOfReturns(any(), any(), any(), anyList());
        //ACT
        sut.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDTOS);

        //VERIFY
        verify(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);
    }

    @Test
    void calculateFrequencyOfReturns_verifycalculateFrequencyIfBinIntervalEqualsMIN2() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var returnsMIN = TWO;
        final var binInterval = TWO;
        final List<DistributionRangeResDTO> rangeResDTOS = List.of();

        doCallRealMethod().when(sut).calculateFrequencyOfReturns(any(), any(), any(), anyList());
        //ACT
        sut.calculateFrequencyOfReturns(returns, returnsMIN, binInterval, rangeResDTOS);

        //VERIFY
        verify(sut).calculateFrequencyIfBinIntervalEqualsMin(returns, returnsMIN);
    }

    @Test
    void initializeDistributionRangeDTO_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var binIndex = 4;
        final var binInterval = TWO;
        final long frequencyOfReturns = 10L;

        doCallRealMethod().when(sut).initializeDistributionRangeDTO(anyInt(), any(), anyLong());
        //ACT
        final DistributionRangeResDTO actual = sut.initializeDistributionRangeDTO(binIndex, binInterval, frequencyOfReturns);

        //VERIFY
        final var expected = new DistributionRangeResDTO(binIndex, binInterval, frequencyOfReturns);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void initializeResponseDTO_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var calculatedMonthlyReturns = new DistributionOfReturnsIntervalResDTO(ONE, TWO, 10, TWELVE, List.of());
        final var calculatedAnnualReturns = new DistributionOfReturnsIntervalResDTO();

        doCallRealMethod().when(sut).initializeResponseDTO(any(), any(), any());
        //ACT
        final DistributionOfReturnsResDTO actual = sut.initializeResponseDTO(calculatedMonthlyReturns, calculatedAnnualReturns, returns);

        //VERIFY
        final var expected = new DistributionOfReturnsResDTO(calculatedMonthlyReturns, calculatedAnnualReturns);
        Assertions.assertEquals(expected.getMonthlyReturns().getDistributionIncrement(), actual.getMonthlyReturns().getDistributionIncrement());
    }

    @Test
    void calculateFrequencyIfBinIntervalEqualsMIN_checkResult() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var portfolioTotalReturns = getPortfolioTotalReturns();
        final var returnsMIN = HUNDRED;

        doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);
        //ACT
        final long actual = sut.calculateFrequencyIfBinIntervalEqualsMin(portfolioTotalReturns, returnsMIN);

        //VERIFY
        Assertions.assertEquals(3, actual);
    }

    @Test
    void calculateFrequencyIfBinIntervalNotEqualsMIN_verifyCalculateFrequencyIfBinIntervalNotEqualsMIN() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var binInterval = HUNDRED;
        final List<DistributionRangeResDTO> rangeResDTOS = List.of();

        doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
        //ACT
        sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);

        //VERIFY
        verify(sut).calculateFrequencyIfBinIntervalEqualsMin(returns, binInterval);
    }

    @Test
    void calculateFrequencyIfBinIntervalNotEqualsMIN_verifyCalculateFrequencyIfBinIntervalNotEqualsMIN2() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var binInterval = HUNDRED;
        final var previousBin = mock(DistributionRangeResDTO.class);
        final var rangeOfPreviousBin = TEN;
        final List<DistributionRangeResDTO> rangeResDTOS = List.of(mock(DistributionRangeResDTO.class), previousBin);

        when(previousBin.getRange()).thenReturn(rangeOfPreviousBin);

        doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(any(), any(), anyList());
        //ACT
        sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);

        //VERIFY
        verify(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
    }

    @Test
    void calculateFrequencyIfBinIntervalNotEqualsMIN_checkResult2() {
        //SETUP
        final var sut = mock(DistributionOfReturnsCalculation.class);
        final var returns = getPortfolioTotalReturns();
        final var rangeOfPreviousBin = TWO;
        final var binInterval = HUNDRED;

        doCallRealMethod().when(sut).calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
        //ACT
        final long actual = sut.calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);

        //VERIFY
        Assertions.assertEquals(2, actual);
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