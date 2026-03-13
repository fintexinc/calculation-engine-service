package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.AverageMerCommand;
import com.fintex.ce.port.input.result.AverageMerResult;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_MER_AMF_001;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_MER_MER_001;
import static com.fintex.ce.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.domain.enumeration.ParameterType.SCALED;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class MERCalculationServiceImplTest {

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

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
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

    final var reqDTO = mock(AverageMerCommand.class);
    final var holdings = mock(List.class);
    final var resDto = mock(AverageMerResult.class);
    final var providers = List.of(DataProvider.MORNINGSTAR);

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getDataProviders()).thenReturn(providers);
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(averageMERCacheStorage).load(holdings, providers, List.of(), new ParamHolderDTO());
  }

  @Test
  void shouldPerform_whenVerifyResDTOSetWarnings() {
    // SETUP
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

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
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

    final var reqDTO = mock(AverageMerCommand.class);
    final var resDTO = mock(AverageMerResult.class);
    final var averageMerCalculationDtos = mock(Map.class);

    when(averageMERCacheStorage.load(any(), any(), any(), any())).thenReturn(averageMerCalculationDtos);
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDTO);

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).setInitialFeeAndModifiedFeeValues(averageMerCalculationDtos);

  }

  @Test
  void shouldPerform_whenVerifyGetResultAndSetNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds() {
    // SETUP
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

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
    final var averageMERCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

    final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
    final var reqDTO = mock(AverageMerCommand.class);
    final var parameterTypes = List.of(SCALED, ABSOLUTE);

    when(sut.loadDataFromCacheStorage(any())).thenReturn(map);
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
      final var averageMERCacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

      final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
      when(averageMERCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
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
      final var averageMERCacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(MERCalculationServiceImpl.class, withSettings().useConstructor(averageMERCacheStorage));

      final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var providers = mock(List.class);

      when(reqDTO.getDataProviders()).thenReturn(providers);
      when(averageMERCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResult.class));

      doCallRealMethod().when(sut).perform(any());
      doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, DataProvider.DEFAULT_PROVIDERS));
    }
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForCanadaEtfAndCanadaMutualFundsWithCanadaEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final Holding h = mock(Holding.class);
    final AverageManagementExpenseCalculationDTO a = new AverageManagementExpenseCalculationDTO();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.CANADA_ETF, Map.of(h, a)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForCanadaEtfAndCanadaMutualFundsWithUsEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final Holding h = mock(Holding.class);
    final AverageManagementExpenseCalculationDTO a = new AverageManagementExpenseCalculationDTO();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.CANADA_MUTUAL_FUNDS, Map.of(h, a)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCallsSetForUsEtfType() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final Holding h = mock(Holding.class);
    final AverageManagementExpenseCalculationDTO aDto = new AverageManagementExpenseCalculationDTO();

    doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.US_ETF, Map.of(h, aDto)));

    // VERIFY
    verify(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(aDto, h, notification);
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCheckResult() {
    // SETUP
    final MERCalculationServiceImpl m = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final Holding h1 = mock(Holding.class);
    final AverageManagementExpenseCalculationDTO aDto1 = mock(AverageManagementExpenseCalculationDTO.class);
    final Holding h2 = mock(Holding.class);
    final AverageManagementExpenseCalculationDTO aDto2 = mock(AverageManagementExpenseCalculationDTO.class);

    final Warning w1 = new Warning(null, "ANY1");
    final Warning w2 = new Warning(null, "ANY2");

    when(m.handleFeesForUsEtfAndMutualFund(aDto1, h1, notification)).thenReturn(Optional.of(w1));
    when(m.handleFeesForCanadaMutualHedgeFundsAndEtf(aDto2, h2, notification)).thenReturn(Optional.of(List.of(w2)));

    doCallRealMethod().when(m).setInitialFeeAndModifiedFeeValues(anyMap());
    // ACT
    final List<Warning> actual = m.setInitialFeeAndModifiedFeeValues(Map.of(
        HoldingType.US_ETF, Map.of(h1, aDto1),
        HoldingType.CANADA_ETF, Map.of(h2, aDto2),
        HoldingType.CANADA_STOCKS, Map.of(mock(Holding.class), mock(AverageManagementExpenseCalculationDTO.class))));

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareCollections(List.of(w2, w1), actual);
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenCallsFillFeeValuesWithManagementExpenseRation() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final AverageManagementExpenseCalculationDTO etfHoldingDto = new AverageManagementExpenseCalculationDTO();
    etfHoldingDto.setHoldingType(HoldingType.CANADA_ETF);

    final BigDecimal mockManagementExpenseRatio = mock(BigDecimal.class);
    etfHoldingDto.setManagementExpenseRatio(mockManagementExpenseRatio);
    etfHoldingDto.setActualManagementFee(mock(BigDecimal.class));

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
    // ACT
    merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(Holding.class),
        notification);

    // VERIFY
    verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockManagementExpenseRatio);
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenThrowsException() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
    // ACT
    merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(mock(
        AverageManagementExpenseCalculationDTO.class), h, notification);

    // VERIFY
    assertEquals(1, notification.getErrors().stream().filter(e -> e.getMessage().equals(
        "The holding is missing both MER and Management Fee")).count());
    verify(h).generateUserIdentifier();
  }

  @Test
  void shouldSetForUsEtfType_whenThrowsException() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
    // ACT
    merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(mock(AverageManagementExpenseCalculationDTO.class), h,
        notification);

    // VERIFY
    assertEquals(1, notification.getErrors().stream().filter(e -> e.getMessage().equals(
        "The holding is missing both Net Expense Ratio and Gross Expense Ratio")).count());
    verify(h).generateUserIdentifier();
    // assertEquals("The holding is missing both Net Expense Ratio and Gross Expense Ratio", e.getMessage());
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenMerIsPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
    when(a.getManagementExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
    // ACT
    final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(a, h,
        notification);

    // VERIFY
    verify(a, times(3)).getManagementExpenseRatio();
    assertFalse(warning.isEmpty());
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenMerIsNotPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
    when(a.getActualManagementFee()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
    // ACT
    final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(a, h,
        notification);

    // VERIFY
    verify(h).generateUserIdentifier();
    verify(a, times(2)).getActualManagementFee();
    assertTrue(warning.isPresent());
    assertEquals(List.of(new Warning(null, "The holding is missing Management Expense Ratio", "WRN_MER_MER_001")),
        warning.get());
  }

  @Test
  void shouldSetForUsEtfType_whenNetIsPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
    when(a.getNetExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
    // ACT
    final Optional<Warning> warning = merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(a, h, notification);

    // VERIFY
    verify(a, times(3)).getNetExpenseRatio();
    assertFalse(warning.isEmpty());
  }

  @Test
  void shouldSetForUsEtfType_whenNetIsNotPresent() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final Holding h = mock(Holding.class);

    final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
    when(a.getGrossExpenseRatio()).thenReturn(ONE);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
    // ACT
    final Optional<Warning> warning = merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(a, h, notification);

    // VERIFY
    verify(h).generateUserIdentifier();
    verify(a, times(2)).getGrossExpenseRatio();
    assertTrue(warning.isPresent());
    assertEquals(new Warning(null, "The holding is missing Net Expense Ratio", "WRN_MER_NER_001"), warning.get());
  }

  @Test
  void shouldSetForCanadaEtfAndCanadaMutualFundTypes_whenCallsFillFeeValuesWithActualManagementFee() {
    // SETUP
    final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();
    final AverageManagementExpenseCalculationDTO etfHoldingDto = new AverageManagementExpenseCalculationDTO();
    etfHoldingDto.setHoldingType(HoldingType.CANADA_ETF);
    final BigDecimal mockActualManagementFee = mock(BigDecimal.class);
    etfHoldingDto.setActualManagementFee(mockActualManagementFee);

    doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
    // ACT
    merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(Holding.class),
        notification);

    // VERIFY
    verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockActualManagementFee);
  }

  @Test
  void shouldCalculateAverageMER_whenVerifyGetScaledAverageMer() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

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
    final Notification notification = new Notification();

    final var holding = mock(Holding.class);
    final var input = mock(AverageManagementExpenseCalculationDTO.class);

    final BigDecimal bigDecimal = mock(BigDecimal.class);
    when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
    when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

    doCallRealMethod().when(sut).handleFeesForUsEtfAndMutualFund(any(), any(), any());

    // ACT
    sut.handleFeesForUsEtfAndMutualFund(input, holding, notification);

    // VERIFY
    verify(sut).setFeeValues(eq(input), same(bigDecimal));
  }

  @Test
  void shouldHandleFeesForUsEtf_whenCheckResult() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final var holding = mock(Holding.class);
    final var input = mock(AverageManagementExpenseCalculationDTO.class);

    final BigDecimal bigDecimal = mock(BigDecimal.class);
    when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
    when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

    doCallRealMethod().when(sut).handleFeesForUsEtfAndMutualFund(any(), any(), any());

    // ACT
    final Optional<Warning> actual = sut.handleFeesForUsEtfAndMutualFund(input, holding, notification);

    // VERIFY
    assertEquals(Optional.empty(), actual);
  }

  @Test
  void shouldHandleFeesForCanadaMutualHedgeFundsAndEtf_whenReturnsTwoWarningsInCaseOfAbsentDataForCanadaHedgeFund() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);
    final Notification notification = new Notification();

    final var holding = new Holding();
    holding.setType(HoldingType.CANADA_HEDGE_FUNDS);
    final var input = mock(AverageManagementExpenseCalculationDTO.class);
    final Optional<List<Warning>> expected = Optional.of(List.of(WRN_MER_MER_001.warning(holding), WRN_MER_AMF_001
        .warning(holding)));

    doCallRealMethod().when(sut).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());

    // ACT
    final Optional<List<Warning>> actual = sut.handleFeesForCanadaMutualHedgeFundsAndEtf(input, holding, notification);

    // VERIFY
    assertEquals(expected, actual);
  }
}