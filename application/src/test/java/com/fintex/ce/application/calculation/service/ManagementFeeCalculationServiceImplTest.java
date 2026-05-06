package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MANAGEMENT_FEE;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType.ETF_CANADA;
import static com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType.ETF_US;
import static com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType.MUTUAL_FUND_CANADA;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
        .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

    final var result = mock(ManagementFeeResult.class);

    when(service.calculateAverageValue(any(), any())).thenReturn(result);

    doCallRealMethod().when(service).perform(any());
    // ACT
    final var actual = service.perform(mock(AverageMerCommand.class));

    // VERIFY
    assertSame(result, actual);
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
          .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

      final var command = mock(AverageMerCommand.class);
      final List<PortfolioHolding> holdings = List.of();
      final var result = mock(ManagementFeeResult.class);
      final var defaultProviders = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), anyList()))
          .thenReturn(defaultProviders);
      when(command.getHoldings()).thenReturn(holdings);
      when(service.calculateAverageValue(any(), any())).thenReturn(result);
      when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(service).perform(any());
      doCallRealMethod().when(service).fetchData(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(feesFetcher).fetch(holdings, defaultProviders);
    }
  }

  @Test
  void shouldPerform_whenVerifySetNullForScaledIfHoldingContainsNoFunds() {
    // SETUP
    final var feesFetcher = mock(SecurityDataFetcher.class);
    final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
        .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

    final var result = mock(ManagementFeeResult.class);
    final var command = mock(AverageMerCommand.class);
    final var managementFee = mock(Map.class);

    when(result.getManagementFee()).thenReturn(managementFee);
    when(service.calculateAverageValue(any(), any())).thenReturn(result);

    doCallRealMethod().when(service).perform(any());
    doCallRealMethod().when(service).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(
        (ManagementFeeResult) any(), (AverageMerCommand) any());
    // ACT
    service.perform(command);

    // VERIFY
    verify(service).setNullForScaledIfHoldingContainsNoFunds(managementFee, command);

  }

  @Test
  void shouldPerform_whenVerifyCalculateAverageValue() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
          .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

      final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var command = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(feesFetcher.fetch(any(), any())).thenReturn(map);
      mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any(ParameterType[].class)))
          .thenReturn(parameterTypes);
      when(service.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

      doCallRealMethod().when(service).perform(any());
      doCallRealMethod().when(service).calculateAverageValue(any(), any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).calculateAverageValue(parameterTypes, map);
    }
  }

  @Test
  void shouldPerform_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
          .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

      final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var command = mock(AverageMerCommand.class);
      final var parameterTypes = mock(List.class);

      when(command.getParameterTypes()).thenReturn(parameterTypes);
      when(feesFetcher.fetch(any(), any())).thenReturn(map);
      when(service.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(parameterTypes, SCALED, ABSOLUTE));
    }
  }

  @Test
  void shouldPerform_whenVerifyGetSpecifiedIfEmptyDEFAULTDATAPROVIDERS() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var feesFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
          .useConstructor(feesFetcher, DEFAULT_DATA_PROPERTIES));

      final HashMap<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
      final var command = mock(AverageMerCommand.class);
      final var providers = mock(List.class);

      when(command.getDataProviders()).thenReturn(providers);
      when(command.getHoldings()).thenReturn(List.of());
      when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(service.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResult.class));

      doCallRealMethod().when(service).perform(any());
      doCallRealMethod().when(service).fetchData(any());
      // ACT
      service.perform(command);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, List.of(DataProvider.MORNINGSTAR)));
    }
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenCheckResult() {
    // SETUP
    final var service = mock(ManagementFeeCalculationServiceImpl.class);

    final var calculationDtoMap = getCalculationDtoMap();
    calculationDtoMap.get(MUTUAL_FUND_CANADA).forEach((key, value) -> value.setActualManagementFee(TEN));
    calculationDtoMap.get(ETF_US).forEach((key, value) -> value.setActualManagementFee(ONE));
    calculationDtoMap.get(ETF_CANADA).forEach((key, value) -> value.setActualManagementFee(ZERO));

    doCallRealMethod().when(service).setFeeValues(any(), any());
    doCallRealMethod().when(service).setInitialFeeAndModifiedFeeValues(any());
    doCallRealMethod().when(service).validateManagementFee(any(), any());
    // ACT
    service.setInitialFeeAndModifiedFeeValues(calculationDtoMap);

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
    final var service = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(ABSOLUTE, SCALED);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();
    final var expected = new ManagementFeeResult(Map.of(SCALED, TEN, ABSOLUTE, ONE));
    when(service.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);
    when(service.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

    doCallRealMethod().when(service).calculateAverageValue(any(), any());
    // ACT
    final var actual = service.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateAverageValue_whenVerifyGetAbsoluteAverageMer() {
    // SETUP
    final var service = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(ABSOLUTE);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();

    when(service.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);

    doCallRealMethod().when(service).calculateAverageValue(any(), any());
    // ACT
    service.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    verify(service).getAbsoluteAverageMer(averageMerCalculationDtoMap);
  }

  @Test
  void shouldCalculateAverageValue_whenVerifyGetScaledAverageMer() {
    // SETUP
    final var service = mock(ManagementFeeCalculationServiceImpl.class);

    final var parameterTypes = List.of(SCALED);
    final var averageMerCalculationDtoMap = getCalculationDtoMap();

    when(service.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

    doCallRealMethod().when(service).calculateAverageValue(any(), any());
    // ACT
    service.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

    // VERIFY
    verify(service).getScaledAverageMer(averageMerCalculationDtoMap);
  }

  private Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> getCalculationDtoMap() {
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto1 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("10"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto2 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("20"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto3 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("30"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto4 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("40"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto5 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("50"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto6 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("60"));
    return Map.of(MUTUAL_FUND_CANADA, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto1),
        ETF_US, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto2),
        ETF_CANADA, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto3),
        FinancialInstrumentType.STOCK_CANADA, Map.of(mock(PortfolioHolding.class),
            averageManagementExpenseCalculationDto4),
        FinancialInstrumentType.STOCK_US, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto5),
        FinancialInstrumentType.CASH, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto6));
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenExpectExceptionWhenHoldingIsFundAndManagementFeeIsEmpty() {
    // SETUP
    var service = mock(ManagementFeeCalculationServiceImpl.class);
    var holding = new PortfolioHolding(null, null, null);
    var averageCalculation = AverageManagementExpenseCalculation.ofActualManagementFee(null);
    var expected = MISSING_MANAGEMENT_FEE.toExceptionForHolding(holding);

    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(MUTUAL_FUND_CANADA, Map.of(holding, averageCalculation));

    doCallRealMethod().when(service).validateManagementFee(any(), any());
    doCallRealMethod().when(service).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    var actualException = assertThrows(CalculationException.class, () -> service.setInitialFeeAndModifiedFeeValues(map));

    // VERIFY
    assertEquals(expected.getErrorCode(), actualException.getErrorCode());
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenNothingHappensIfHoldingsIsOnlyCashAndStocks() {
    // SETUP
    var service = mock(ManagementFeeCalculationServiceImpl.class);
    var holding = new PortfolioHolding(null, null, null);
    var averageCalculation = AverageManagementExpenseCalculation.ofActualManagementFee(null);
    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(FinancialInstrumentType.STOCK_US, Map.of(holding, averageCalculation));

    doCallRealMethod().when(service).validateManagementFee(any(), any());
    doCallRealMethod().when(service).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    List<Warning> warnings = service.setInitialFeeAndModifiedFeeValues(map);

    // VERIFY
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldSetInitialFeeAndModifiedFeeValues_whenNothingHappensIfHoldingsIsFundAndContainsManagementFee() {
    // SETUP
    var service = mock(ManagementFeeCalculationServiceImpl.class);
    var holding = new PortfolioHolding(null, null, null);
    var averageCalculation = AverageManagementExpenseCalculation.ofActualManagementFee(TEN);
    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> map = new HashMap<>();
    map.put(ETF_US, Map.of(holding, averageCalculation));

    doCallRealMethod().when(service).validateManagementFee(any(), any());
    doCallRealMethod().when(service).setInitialFeeAndModifiedFeeValues(any());
    // ACT
    List<Warning> warnings = service.setInitialFeeAndModifiedFeeValues(map);

    // VERIFY
    assertTrue(warnings.isEmpty());
  }

}