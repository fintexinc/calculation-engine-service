package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AverageManagementExpenseCalculationServiceTest {

  @Test
  void shouldSetFeeValues_whenCheckResult() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final var req = mock(AverageManagementExpenseCalculation.class);

    doCallRealMethod().when(service).setFeeValues(any(), any());
    // ACT

    service.setFeeValues(req, TEN);

    // VERIFY
    verify(req).setInitialFee(TEN);
    verify(req).setModifiedFee(TEN);
  }

  @Test
  void shouldGetScaledAverageMer_whenCallsGetAbsoluteAndForceReportFeeHoldingList() {
    // SETUP
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final var service = mock(AverageManagementExpenseCalculationService.class);
    doCallRealMethod().when(service).getScaledAverageMer(anyMap());

    // ACT
    service.getScaledAverageMer(holdings);

    // VERIFY
    verify(service).getAbsoluteAndForceReportFeeHoldingList(holdings);
  }

  @Test
  void shouldGetScaledAverageMer_whenWhenParameterTypeAbsoluteCallsGetAverageMerByParameterTypeWithAllFinancialInstrumentTypes() {
    // SETUP
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> absoluteHoldings = Stream.of(holdings.get(
        FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
        holdings.get(
            FinancialInstrumentType.ETF_US),
        holdings.get(FinancialInstrumentType.ETF_CANADA), holdings.get(FinancialInstrumentType.STOCK_US), holdings.get(
            FinancialInstrumentType.STOCK_CANADA), holdings.get(FinancialInstrumentType.CASH))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    doCallRealMethod().when(service).getAbsoluteAverageMer(anyMap());

    // ACT
    service.getAbsoluteAverageMer(holdings);

    // VERIFY
    verify(service).getAverageMerByParameterType(absoluteHoldings);
  }

  @Test
  void shouldGetAbsoluteAndForceReportFeeHoldingList_whenIsPappedProperly() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(service).getAbsoluteAndForceReportFeeHoldingList(anyMap());
    final List<AverageManagementExpenseCalculation> absoluteOrForceReportFeeHoldings = Stream.of(holdings.get(
        FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
        holdings.get(
            FinancialInstrumentType.ETF_US), holdings.get(FinancialInstrumentType.ETF_CANADA))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    // ACT
    final List<AverageManagementExpenseCalculation> result = service.getAbsoluteAndForceReportFeeHoldingList(holdings);

    // VERIFY
    assertEquals(absoluteOrForceReportFeeHoldings, result);
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculateAverageManagementExpenseRatio() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDtoList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(service).getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // ACT
    service.getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // VERIFY
    verify(service, times(2)).calculateAverageManagementExpenseRatio(any(), any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculateMarketValueQualified() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDtoList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(service).getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // ACT
    service.getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // VERIFY
    verify(service, times(2)).calculateMarketValueQualified(any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculatePercentageQualified() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDtoList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(service).getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // ACT
    service.getAverageMerByParameterType(averageManagementExpenseCalculationDtoList);

    // VERIFY
    verify(service, times(2)).calculatePercentageQualified(any(), any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsGetAmountOfMarketValueQualified() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    doCallRealMethod().when(service).getAverageMerByParameterType(anyList());

    // ACT
    service.getAverageMerByParameterType(anyList());

    // VERIFY
    verify(service).getAmountOfMarketValues(anyList());
  }

  @Test
  void shouldGetScaledAverageMer_whenWhenParameterTypeScaledCallsGetAverageMerByParameterTypeWithCanadaMutualFundsUsEtfAndCanadaEtfHoldings() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);

    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(service).getScaledAverageMer(anyMap());
    doCallRealMethod().when(service).getAbsoluteAndForceReportFeeHoldingList(anyMap());

    // ACT
    service.getScaledAverageMer(holdings);

    // VERIFY
    verify(service).getAverageMerByParameterType(
        Stream.of(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(
            FinancialInstrumentType.SEGREGATED_FUND_CANADA),
            holdings.get(FinancialInstrumentType.ETF_US), holdings.get(FinancialInstrumentType.ETF_CANADA))
            .map(Map::values).flatMap(Collection::stream)
            .toList());
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenWhenParameterTypeForceReportFeeCallsGetAbsoluteAndForceReportFeeHoldingList() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);

    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(service).getForceReportFeeAverageMer(anyMap());

    // ACT
    service.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(service).getAbsoluteAndForceReportFeeHoldingList(holdings);
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenWhenParameterTypeForceReportFeeCallsGetAverageMerByParameterTypeWithCanadaMutualFundsUsEtfAndCanadaEtfHoldings() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(service).getForceReportFeeAverageMer(anyMap());
    doCallRealMethod().when(service).getAbsoluteAndForceReportFeeHoldingList(anyMap());

    // ACT
    service.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(service).getAverageMerByParameterType(Stream.of(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA),
        holdings.get(
            FinancialInstrumentType.SEGREGATED_FUND_CANADA), holdings.get(FinancialInstrumentType.ETF_US), holdings.get(
                FinancialInstrumentType.ETF_CANADA))
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList());
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenCallsIsMerPresentForAHolding4Times() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(service).getForceReportFeeAverageMer(anyMap());

    // ACT
    service.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(service, times(6)).isMerPresentForHolding(any(), any());
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenReturnsNullWhenMerValueIsNull() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final Function<AverageManagementExpenseCalculation, BigDecimal> expenseRatioFunction = AverageManagementExpenseCalculation::getManagementExpenseRatio;
    when(service.isMerPresentForHolding(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA), expenseRatioFunction))
        .thenReturn(
            true);

    doCallRealMethod().when(service).getForceReportFeeAverageMer(anyMap());
    // ACT
    final BigDecimal forceReportFeeAverageMer = service.getForceReportFeeAverageMer(holdings);

    // VERIFY
    assertNull(forceReportFeeAverageMer);
  }

  @Test
  void shouldIsMerPresentForAHolding_whenReturnsTrueIfMerIsAbsent() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);

    final Map<PortfolioHolding, AverageManagementExpenseCalculation> mockAverageMerCalculationDtoList = Map.of(mock(
        PortfolioHolding.class), new AverageManagementExpenseCalculation());
    final Function<AverageManagementExpenseCalculation, BigDecimal> functionMock = mock(Function.class);
    doCallRealMethod().when(service).isMerPresentForHolding(any(), any());

    // ACT
    final boolean presentForAHolding = service.isMerPresentForHolding(mockAverageMerCalculationDtoList, functionMock);

    // VERIFY
    assertTrue(presentForAHolding);
  }

  @Test
  void shouldIsMerPresentForAHolding_whenReturnsFalseIfMerIsPresent() {
    // SETUP
    final var service = mock(MERCalculationServiceImpl.class);

    final AverageManagementExpenseCalculation mockAverageManagementExpenseCalculation = mock(
        AverageManagementExpenseCalculation.class);
    when(mockAverageManagementExpenseCalculation.getManagementExpenseRatio()).thenReturn(BigDecimal.ZERO);
    final Map<PortfolioHolding, AverageManagementExpenseCalculation> mockAverageMerCalculationDtoList = Map.of(mock(
        PortfolioHolding.class), mockAverageManagementExpenseCalculation);

    doCallRealMethod().when(service).isMerPresentForHolding(any(), any());
    // ACT
    final boolean presentForAHolding = service.isMerPresentForHolding(
        mockAverageMerCalculationDtoList, AverageManagementExpenseCalculation::getManagementExpenseRatio);

    // VERIFY
    assertFalse(presentForAHolding);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperly() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .modifiedFee(new BigDecimal("0.97"))
        .percentageQualified(new BigDecimal("34"))
        .build();
    doCallRealMethod().when(service).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = service.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculation);

    // VERIFY
    assertEquals(new BigDecimal("62.98"), result);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperlyWhenModifiedFeeIsNull() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .percentageQualified(new BigDecimal("34"))
        .build();
    doCallRealMethod().when(service).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = service.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculation);

    // VERIFY
    assertEquals(new BigDecimal("30"), result);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperlyWhenPercentageQualifiedIsNull() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .modifiedFee(new BigDecimal("0.97"))
        .build();
    doCallRealMethod().when(service).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = service.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculation);

    // VERIFY
    assertEquals(new BigDecimal("30"), result);
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDtoisMappedProperly() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .marketValueQualified(new BigDecimal("50000"))
        .build();
    doCallRealMethod().when(service).calculatePercentageQualified(any(), any());

    // ACT
    service.calculatePercentageQualified(averageManagementExpenseCalculation, new BigDecimal("70000"));

    // VERIFY
    assertEquals(new BigDecimal("0.714285714285714"), averageManagementExpenseCalculation.getPercentageQualified());
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDtoisMappedProperlyWhenMarketValueQualifiedIsNull() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = new AverageManagementExpenseCalculation();

    // ACT
    service.calculatePercentageQualified(averageManagementExpenseCalculation, new BigDecimal("70000"));

    // VERIFY
    assertNull(averageManagementExpenseCalculation.getPercentageQualified());
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDtoisMappedProperlyWhenMarketValueQualifiedIsZero() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .marketValueQualified(BigDecimal.ZERO)
        .build();

    // ACT
    service.calculatePercentageQualified(averageManagementExpenseCalculation, new BigDecimal("70000"));

    // VERIFY
    assertNull(averageManagementExpenseCalculation.getPercentageQualified());
  }

  @Test
  void shouldCalculateMarketValueQualified_whenIsMappedProperlyWhenModifiedFeeValueExists() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = AverageManagementExpenseCalculation
        .builder()
        .modifiedFee(new BigDecimal("100"))
        .marketValue(new BigDecimal("10000"))
        .build();
    doCallRealMethod().when(service).calculateMarketValueQualified(averageManagementExpenseCalculation);

    // ACT
    service.calculateMarketValueQualified(averageManagementExpenseCalculation);

    // VERIFY
    assertEquals(averageManagementExpenseCalculation.getMarketValue(), averageManagementExpenseCalculation
        .getMarketValueQualified());
  }

  @Test
  void shouldCalculateMarketValueQualified_whenIsMappedProperlyWhenModifiedFeeValueIsNull() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculation = new AverageManagementExpenseCalculation();
    doCallRealMethod().when(service).calculateMarketValueQualified(averageManagementExpenseCalculation);

    // ACT
    service.calculateMarketValueQualified(averageManagementExpenseCalculation);

    // VERIFY
    assertEquals(new BigDecimal(BigInteger.ZERO), averageManagementExpenseCalculation.getMarketValueQualified());
  }

  @Test
  void shouldGetAmountOfMarketValueQualified_whenReturnsCorrectValue() {
    // SETUP
    final var service = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto1 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("10"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto2 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("20"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto3 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("30"));
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDtoList = List.of(
        averageManagementExpenseCalculationDto1, averageManagementExpenseCalculationDto2,
        averageManagementExpenseCalculationDto3);
    doCallRealMethod().when(service).getAmountOfMarketValues(any());

    // ACT
    final BigDecimal amountOfMarketValueQualified = service.getAmountOfMarketValues(
        averageManagementExpenseCalculationDtoList);

    // VERIFY
    assertEquals(new BigDecimal("60"), amountOfMarketValueQualified);
  }

  @Test
  void shouldSetNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds_whenCheckResult2() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP

      final var service = mock(AverageManagementExpenseCalculationService.class);

      final var command = mock(AverageMerCommand.class);
      final var result = new AverageMerResult();
      result.getManagementExpenseRatio().put(ABSOLUTE, ONE);
      result.getManagementExpenseRatio().put(SCALED, TEN);
      result.getManagementExpenseRatio().put(FORCE_REPORT_FEE, ZERO);
      final var holding1 = new PortfolioHolding(null, FinancialInstrumentType.STOCK_US, null);
      final var holding2 = new PortfolioHolding(null, FinancialInstrumentType.CASH, null);
      final var holdings = List.of(holding1, holding2);
      final var expected = new HashMap<ParameterType, BigDecimal>();
      expected.put(ABSOLUTE, ONE);
      expected.put(SCALED, null);
      expected.put(FORCE_REPORT_FEE, null);

      when(command.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(service).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(anyMap(), any());
      doCallRealMethod().when(service).setNullForScaledIfHoldingContainsNoFunds(any(), any());
      doCallRealMethod().when(service).setNullForForcedReportFeeIfHoldingContainsNoFunds(any(), any());
      // ACT
      service.setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(result.getManagementExpenseRatio(), command);

      // VERIFY
      Assertions.assertNotNull(result);
      ComparisonUtils.compareMaps(expected, result.getManagementExpenseRatio());
    }
  }

  private Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> getAverageMerCalculationDtoMap() {
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
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDto7 = AverageManagementExpenseCalculation
        .ofMarketValue(new BigDecimal("70"));
    return Map.of(FinancialInstrumentType.MUTUAL_FUND_CANADA, Map.of(mock(PortfolioHolding.class),
        averageManagementExpenseCalculationDto1),
        FinancialInstrumentType.ETF_US, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto2),
        FinancialInstrumentType.ETF_CANADA, Map.of(mock(PortfolioHolding.class),
            averageManagementExpenseCalculationDto3),
        FinancialInstrumentType.STOCK_CANADA, Map.of(mock(PortfolioHolding.class),
            averageManagementExpenseCalculationDto4),
        FinancialInstrumentType.STOCK_US, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto5),
        FinancialInstrumentType.CASH, Map.of(mock(PortfolioHolding.class), averageManagementExpenseCalculationDto6),
        FinancialInstrumentType.SEGREGATED_FUND_CANADA, Map.of(mock(PortfolioHolding.class),
            averageManagementExpenseCalculationDto7));
  }

}