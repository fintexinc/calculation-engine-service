package com.fintex.ce.application.calculation.service;

import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculation;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.ManagementFeeResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.domain.model.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.SCALED;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_US;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.MUTUAL_FUND_CANADA;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class ManagementFeeCalculationServiceImplTest {

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
            .useConstructor(feesFetcher));

    final var resDto = mock(ManagementFeeResult.class);

    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final var actual = sut.perform(mock(AverageMerCommand.class));

    // VERIFY
    assertSame(resDto, actual);
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
              .useConstructor(feesFetcher));

      final var reqDTO = mock(AverageMerCommand.class);
      final List<Holding> holdings = List.of();
      final var resDto = mock(ManagementFeeResult.class);
      final var defaultProviders = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any(DataProvider[].class)))
              .thenReturn(defaultProviders);
      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);
      when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(sut).perform(any());
      doCallRealMethod().when(sut).fetchData(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(feesFetcher).fetch(holdings, defaultProviders);
    }
  }

  @Test
  void shouldPerform_whenVerifySetNullForScaledIfHoldingContainsNoFunds() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
            .useConstructor(feesFetcher));

    final var resDto = mock(ManagementFeeResult.class);
    final var reqDTO = mock(AverageMerCommand.class);
    final var managementFee = mock(Map.class);

    when(resDto.getManagementFee()).thenReturn(managementFee);
    when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

    doCallRealMethod().when(sut).perform(any());
    doCallRealMethod().when(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(
            (ManagementFeeResult) any(), (AverageMerCommand) any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).setNullForScaledIfHoldingContainsNoFunds(managementFee, reqDTO);

  }

  @Test
  void shouldPerform_whenVerifyCalculateAverageValue() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
              .useConstructor(feesFetcher));

      final HashMap<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(feesFetcher.fetch(any(), any())).thenReturn(map);
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any(ParameterType[].class)))
              .thenReturn(parameterTypes);
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

      doCallRealMethod().when(sut).perform(any());
      doCallRealMethod().when(sut).calculateAverageValue(any(), any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      verify(sut).calculateAverageValue(parameterTypes, map);
    }
  }

  @Test
  void shouldPerform_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
              .useConstructor(feesFetcher));

      final HashMap<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
      when(feesFetcher.fetch(any(), any())).thenReturn(map);
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

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
      final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
              .useConstructor(feesFetcher));

      final HashMap<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var reqDTO = mock(AverageMerCommand.class);
      final var providers = mock(List.class);

      when(reqDTO.getDataProviders()).thenReturn(providers);
      when(reqDTO.getHoldings()).thenReturn(List.of());
      when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

      doCallRealMethod().when(sut).perform(any());
      doCallRealMethod().when(sut).fetchData(any());
      // ACT
      sut.perform(reqDTO);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, DataProvider.MORNINGSTAR));
    }
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCheckResult() {
    // SETUP
    final var sut = mock(ManagementFeeCalculationServiceImpl.class);

    final var calculationDtoMap = getCalculationDtoMap();
    calculationDtoMap.get(MUTUAL_FUND_CANADA).forEach((key, value) -> value.setActualManagementFee(TEN));
    calculationDtoMap.get(ETF_US).forEach((key, value) -> value.setActualManagementFee(ONE));
    calculationDtoMap.get(ETF_CANADA).forEach((key, value) -> value.setActualManagementFee(ZERO));

    doCallRealMethod().when(sut).setFeeValues(any(), any());
    doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
    doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
    // ACT
    sut.setInitialFeeAndModifiedFeeValues(calculationDtoMap);

    // VERIFY
    // Only FUNDS (children of FUND type) have their fees validated and set
    calculationDtoMap.get(MUTUAL_FUND_CANADA).forEach((key, value) -> {
      assertEquals(TEN, value.getInitialFee());
      assertEquals(TEN, value.getModifiedFee());
    });
    // ETFs are not in FUNDS group, so their fees are not processed by setInitialFeeAndModifiedFeeValues
    calculationDtoMap.get(ETF_US).forEach((key, value) -> {
      assertNull(value.getInitialFee());
      assertNull(value.getModifiedFee());
    });
    calculationDtoMap.get(ETF_CANADA).forEach((key, value) -> {
      assertNull(value.getInitialFee());
      assertNull(value.getModifiedFee());
    });
  }

  @Test
  void shouldCalculateAverageValue_whenCheckResult() {
    // SETUP
    final var sut = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(ABSOLUTE, SCALED);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();
    final var expected = new ManagementFeeResult();
    expected.setManagementFee(Map.of(SCALED, TEN, ABSOLUTE, ONE));

    when(sut.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);
    when(sut.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateAverageValue_whenVerifyGetAbsoluteAverageMer() {
    // SETUP
    final var sut = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(ABSOLUTE);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();

    when(sut.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    verify(sut).getAbsoluteAverageMer(averageMerCalculationDtoMap);
  }

  @Test
  void shouldCalculateAverageValue_whenVerifyGetScaledAverageMer() {
    // SETUP
    final var sut = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(SCALED);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();

    when(sut.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateAverageValue(any(), any());
    // ACT
    sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    verify(sut).getScaledAverageMer(averageMerCalculationDtoMap);
  }

  private Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> getCalculationDtoMap() {
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO1 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO1.setMarketValue(new BigDecimal("10"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO2 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO2.setMarketValue(new BigDecimal("20"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO3 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO3.setMarketValue(new BigDecimal("30"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO4 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO4.setMarketValue(new BigDecimal("40"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO5 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO5.setMarketValue(new BigDecimal("50"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO6 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO6.setMarketValue(new BigDecimal("60"));

    return Map.of(MUTUAL_FUND_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO1),
            ETF_US, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO2),
            ETF_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO3),
            FinancialInstrumentType.STOCK_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO4),
            FinancialInstrumentType.STOCK_US, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO5),
            FinancialInstrumentType.CASH, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO6));
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenExpectExceptionWhenHoldingIsFundAndManagementFeeIsEmpty() {
    // SETUP
    var sut = mock(ManagementFeeCalculationServiceImpl.class);
    var holding = new Holding();
    var averageCalculationDto = new AverageManagementExpenseCalculation();
    averageCalculationDto.setActualManagementFee(null);
    var expected = ERR_MF_MF_001.error(holding);

    Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(MUTUAL_FUND_CANADA, Map.of(holding, averageCalculationDto));

    doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
    doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    var actualException = assertThrows(FdsDataValidationException.class, () -> sut.setInitialFeeAndModifiedFeeValues(
            map));

    // VERIFY
    assertTrue(actualException.getExceptionList().stream().anyMatch(e -> e.getCode().equals(expected.getCode())));
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenNothingHappensIfHoldingsIsOnlyCashAndStocks() {
    // SETUP
    var sut = mock(ManagementFeeCalculationServiceImpl.class);
    var notification = new Notification();
    var holding = new Holding();
    var averageCalculationDto = new AverageManagementExpenseCalculation();
    averageCalculationDto.setActualManagementFee(null);

    Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(FinancialInstrumentType.STOCK_US, Map.of(holding, averageCalculationDto));

    doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
    doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    sut.setInitialFeeAndModifiedFeeValues(map);

    // VERIFY
    assertFalse(notification.hasErrors());
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenNothingHappensIfHoldingsIsFundAndContainsManagementFee() {
    // SETUP
    var sut = mock(ManagementFeeCalculationServiceImpl.class);
    var notification = new Notification();
    var holding = new Holding();
    var averageCalculationDto = new AverageManagementExpenseCalculation();
    averageCalculationDto.setActualManagementFee(TEN);

    Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(ETF_US, Map.of(holding, averageCalculationDto));

    doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
    doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    sut.setInitialFeeAndModifiedFeeValues(map);

    // VERIFY
    assertFalse(notification.hasErrors());
  }

}