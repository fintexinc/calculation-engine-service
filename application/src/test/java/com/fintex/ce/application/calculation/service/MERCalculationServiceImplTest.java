package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.model.error.ErrorCode.MISSING_ACTUAL_MANAGEMENT_FEE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MANAGEMENT_EXPENSE_RATIO;
import static com.fintex.ce.model.error.ErrorCode.MISSING_NET_EXPENSE_RATIO;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class MERCalculationServiceImplTest {

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final var resDto = mock(AverageMerResult.class);

    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final var actual = sut.perform(mock(AverageMerCommand.class));

    // VERIFY
    assertSame(resDto, actual);
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final var reqDTO = mock(AverageMerCommand.class);
    final List<PortfolioHolding> holdings = List.of();
    final var resDto = mock(AverageMerResult.class);
    final var providers = List.of(DataProvider.MORNINGSTAR);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getDataProviders()).thenReturn(providers);
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).fetchData(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(feesFetcher).fetch(holdings, providers);
  }

  @Test
  void shouldPerform_whenVerifyResDTOSetWarnings() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final var reqDTO = mock(AverageMerCommand.class);
    final var resDTO = mock(AverageMerResult.class);
    final var warnings = mock(List.class);

    when(sut.calculateAverageValue(any(), any())).thenReturn(resDTO);
    when(sut.setInitialFeeAndModifiedFeeValues(any())).thenReturn(warnings);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(resDTO).setWarnings(warnings);

  }

  @Test
  void shouldPerform_whenVerifySetInitialFeeAndModifiedFeeValues() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final var reqDTO = mock(AverageMerCommand.class);
    final var resDTO = mock(AverageMerResult.class);

    when(reqDTO.getHoldings()).thenReturn(List.of());
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDTO);

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).fetchData(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).setInitialFeeAndModifiedFeeValues(any());

  }

  @Test
  void shouldPerform_whenVerifyGetResultAndSetNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final var resDto = mock(AverageMerResult.class);
    final var reqDTO = mock(AverageMerCommand.class);
    final var managementExpenseRatio = mock(Map.class);

    when(resDto.getManagementExpenseRatio()).thenReturn(managementExpenseRatio);
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(any(
        AverageMerResult.class), any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(managementExpenseRatio, reqDTO);

  }

  @Test
  void shouldPerform_whenVerifyCalculateAverageMER() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
        DEFAULT_DATA_PROPERTIES));

    final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    final var reqDTO = mock(AverageMerCommand.class);
    final var parameterTypes = List.of(SCALED, ABSOLUTE);

    when(sut.fetchData(any())).thenReturn(map);
    when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
    when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResult.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).calculateAverageValue(parameterTypes, map);
  }

  @Test
  void shouldPerform_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
          DEFAULT_DATA_PROPERTIES));

      final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
      when(feesFetcher.fetch(any(), any())).thenReturn(map);
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResult.class));

      doCallRealMethod().when(sut).perform(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(parameterTypes, SCALED, ABSOLUTE));
    }
  }

  @Test
  void shouldPerform_whenVerifyGetSpecifiedIfEmptyDEFAULTDATAPROVIDERS() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(feesFetcher,
          DEFAULT_DATA_PROPERTIES));

      final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var providers = mock(List.class);

      when(reqDTO.getDataProviders()).thenReturn(providers);
      when(reqDTO.getHoldings()).thenReturn(List.of());
      when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResult.class));

      doCallRealMethod().when(sut).perform(any());
      doCallRealMethod().when(sut).fetchData(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, List.of(DataProvider.MORNINGSTAR)));
    }
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForCanadaEtfAndCanadaMutualFundsWithCanadaEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation a = new AverageManagementExpenseCalculation();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(
        Map.of(FinancialInstrumentType.ETF_CANADA, Map.of(h, a)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(a, h);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForCanadaEtfAndCanadaMutualFundsWithUsEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation a = new AverageManagementExpenseCalculation();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(
        Map.of(FinancialInstrumentType.MUTUAL_FUND_CANADA, Map.of(h, a)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(a, h);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForUsEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final PortfolioHolding h = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation aDto = new AverageManagementExpenseCalculation();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(
        Map.of(FinancialInstrumentType.ETF_US, Map.of(h, aDto)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeeDataForUsEtfAndMutualFund(aDto, h);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCheckResult() {
    // SETUP
    final MERCalculationServiceImpl m = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final PortfolioHolding h1 = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation aDto1 = mock(AverageManagementExpenseCalculation.class);
    final PortfolioHolding h2 = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation aDto2 = mock(AverageManagementExpenseCalculation.class);

    final Warning w1 = new Warning(null, "ANY1");
    final Warning w2 = new Warning(null, "ANY2");

    when(m.handleFeeDataForUsEtfAndMutualFund(aDto1, h1)).thenReturn(Optional.of(w1));
    when(m.handleFeeDataForCanadaMutualHedgeFundsAndEtf(aDto2, h2)).thenReturn(Optional.of(List.of(w2)));

    doCallRealMethod().when(m).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    final List<Warning> actual = m.setInitialFeeAndModifiedFeeValues(Map.of(
        FinancialInstrumentType.ETF_US, Map.of(h1, aDto1),
        FinancialInstrumentType.ETF_CANADA, Map.of(h2, aDto2),
        FinancialInstrumentType.STOCK_CANADA,
        Map.of(mock(PortfolioHolding.class), mock(AverageManagementExpenseCalculation.class))));

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareCollections(List.of(w2, w1), actual);
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenCallsFillFeeValuesWithManagementExpenseRation() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final AverageManagementExpenseCalculation etfHoldingDto = new AverageManagementExpenseCalculation();
    etfHoldingDto.setHoldingType(FinancialInstrumentType.ETF_CANADA);

    final BigDecimal mockManagementExpenseRatio = mock(BigDecimal.class);
    etfHoldingDto.setManagementExpenseRatio(mockManagementExpenseRatio);
    etfHoldingDto.setActualManagementFee(mock(BigDecimal.class));

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());
    // ACT
    merCalculationServiceMock.handleFeeDataForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(PortfolioHolding.class));

    // VERIFY
    verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockManagementExpenseRatio);
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenThrowsException() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PortfolioHolding h = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation dto = mock(AverageManagementExpenseCalculation.class);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());
    // ACT + VERIFY
    CalculationException thrown = assertThrows(CalculationException.class,
        () -> merCalculationServiceMock.handleFeeDataForCanadaMutualHedgeFundsAndEtf(dto, h));
    assertEquals("The holding is missing both MER and Management Fee", thrown.getMessage());
    verify(h).getIdsString();
  }

  @Test
  void shouldSetForUsEtfType_whenThrowsException() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PortfolioHolding h = mock(PortfolioHolding.class);
    final AverageManagementExpenseCalculation dto = mock(AverageManagementExpenseCalculation.class);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForUsEtfAndMutualFund(any(), any());
    // ACT + VERIFY
    CalculationException thrown = assertThrows(CalculationException.class,
        () -> merCalculationServiceMock.handleFeeDataForUsEtfAndMutualFund(dto, h));
    assertEquals("The holding is missing both Net Expense Ratio and Gross Expense Ratio", thrown.getMessage());
    verify(h).getIdsString();
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenMerIsPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();
    final PortfolioHolding h = mock(PortfolioHolding.class);

    final AverageManagementExpenseCalculation a = mock(AverageManagementExpenseCalculation.class);
    when(a.getManagementExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());
    // ACT
    final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeeDataForCanadaMutualHedgeFundsAndEtf(a,
        h);

    // VERIFY
    verify(a, times(3)).getManagementExpenseRatio();
    assertFalse(warning.isEmpty());
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenMerIsNotPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();
    final PortfolioHolding h = mock(PortfolioHolding.class);

    final AverageManagementExpenseCalculation a = mock(AverageManagementExpenseCalculation.class);
    when(a.getActualManagementFee()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());
    // ACT
    final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeeDataForCanadaMutualHedgeFundsAndEtf(a,
        h);

    // VERIFY
    verify(h).getIdsString();
    verify(a, times(2)).getActualManagementFee();
    assertTrue(warning.isPresent());
    assertEquals(List.of(new Warning(null, "The holding is missing Management Expense Ratio",
        MISSING_MANAGEMENT_EXPENSE_RATIO.getCode())), warning.get());
  }

  @Test
  void shouldSetForUsEtfType_whenNetIsPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();
    final PortfolioHolding h = mock(PortfolioHolding.class);

    final AverageManagementExpenseCalculation a = mock(AverageManagementExpenseCalculation.class);
    when(a.getNetExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForUsEtfAndMutualFund(any(), any());
    // ACT
    final Optional<Warning> warning = merCalculationServiceMock.handleFeeDataForUsEtfAndMutualFund(a, h);

    // VERIFY
    verify(a, times(3)).getNetExpenseRatio();
    assertFalse(warning.isEmpty());
  }

  @Test
  void shouldSetForUsEtfType_whenNetIsNotPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();
    final PortfolioHolding h = mock(PortfolioHolding.class);

    final AverageManagementExpenseCalculation a = mock(AverageManagementExpenseCalculation.class);
    when(a.getGrossExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForUsEtfAndMutualFund(any(), any());
    // ACT
    final Optional<Warning> warning = merCalculationServiceMock.handleFeeDataForUsEtfAndMutualFund(a, h);

    // VERIFY
    verify(h).getIdsString();
    verify(a, times(2)).getGrossExpenseRatio();
    assertTrue(warning.isPresent());
    assertEquals(new Warning(null, "The holding is missing Net Expense Ratio",
        MISSING_NET_EXPENSE_RATIO.getCode()), warning.get());
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenCallsFillFeeValuesWithActualManagementFee() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();
    final AverageManagementExpenseCalculation etfHoldingDto = new AverageManagementExpenseCalculation();
    etfHoldingDto.setHoldingType(FinancialInstrumentType.ETF_CANADA);
    final BigDecimal mockActualManagementFee = mock(BigDecimal.class);
    etfHoldingDto.setActualManagementFee(mockActualManagementFee);

    doCallRealMethod().when(merCalculationServiceMock).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());
    // ACT
    merCalculationServiceMock.handleFeeDataForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(PortfolioHolding.class));

    // VERIFY
    verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockActualManagementFee);
  }

  @Test
  void shouldCalculateAverageMER_whenVerifyGetScaledAverageMer() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(SCALED)).thenReturn(true);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    verify(sut).getScaledAverageMer(averageMerCalculationDtos);
  }

  @Test
  void shouldCalculateAverageMER_whenVerifyGetAbsoluteAverageMer() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    verify(sut).getAbsoluteAverageMer(averageMerCalculationDtos);
  }

  @Test
  void shouldCalculateAverageMER_whenVerifyGetForceReportFeeAverageMer() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(FORCE_REPORT_FEE)).thenReturn(true);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    verify(sut).getForceReportFeeAverageMer(averageMerCalculationDtos);
  }

  @Test
  void shouldCalculateAverageMER_whenCheckResult1() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final var expected = new AverageMerResult();
    expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO, ABSOLUTE, ONE, FORCE_REPORT_FEE, TEN));

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(SCALED)).thenReturn(true);
    when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);
    when(parameterTypes.contains(FORCE_REPORT_FEE)).thenReturn(true);

    when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);
    when(sut.getAbsoluteAverageMer(averageMerCalculationDtos)).thenReturn(ONE);
    when(sut.getForceReportFeeAverageMer(averageMerCalculationDtos)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateAverageMER_whenCheckResult2() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final var expected = new AverageMerResult();
    expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO, ABSOLUTE, ONE));

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(SCALED)).thenReturn(true);
    when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);

    when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);
    when(sut.getAbsoluteAverageMer(averageMerCalculationDtos)).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateAverageMER_whenCheckResult3() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final var expected = new AverageMerResult();
    expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO));

    final var parameterTypes = mock(List.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(parameterTypes.contains(SCALED)).thenReturn(true);

    when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldHandleFeesForUsEtf_whenVerifySetFeeValues() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var holding = mock(PortfolioHolding.class);
    final var input = mock(AverageManagementExpenseCalculation.class);

    final BigDecimal bigDecimal = mock(BigDecimal.class);
    when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
    when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

    doCallRealMethod().when(sut).handleFeeDataForUsEtfAndMutualFund(any(), any());

    // ACT
    sut.handleFeeDataForUsEtfAndMutualFund(input, holding);

    // VERIFY
    verify(sut).setFeeValues(eq(input), same(bigDecimal));
  }

  @Test
  void shouldHandleFeesForUsEtf_whenCheckResult() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var holding = mock(PortfolioHolding.class);
    final var input = mock(AverageManagementExpenseCalculation.class);

    final BigDecimal bigDecimal = mock(BigDecimal.class);
    when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
    when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

    doCallRealMethod().when(sut).handleFeeDataForUsEtfAndMutualFund(any(), any());

    // ACT
    final Optional<Warning> actual = sut.handleFeeDataForUsEtfAndMutualFund(input, holding);

    // VERIFY
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void shouldHandleFeesForCanadaMutualHedgeFundsAndEtf_whenReturnsTwoWarningsInCaseOfAbsentDataForCanadaHedgeFund() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var holding = new PortfolioHolding(null, FinancialInstrumentType.HEDGE_FUND_CANADA, null);
    final var input = mock(AverageManagementExpenseCalculation.class);
    final Optional<List<Warning>> expected = Optional.of(List.of(MISSING_MANAGEMENT_EXPENSE_RATIO.warning(holding),
        MISSING_ACTUAL_MANAGEMENT_FEE
            .warning(holding)));

    doCallRealMethod().when(sut).handleFeeDataForCanadaMutualHedgeFundsAndEtf(any(), any());

    // ACT
    final Optional<List<Warning>> actual = sut.handleFeeDataForCanadaMutualHedgeFundsAndEtf(input, holding);

    // VERIFY
    assertEquals(expected, actual);
  }
}