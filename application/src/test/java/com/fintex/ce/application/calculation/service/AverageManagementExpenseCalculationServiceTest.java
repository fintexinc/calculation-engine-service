package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculation;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AverageMerResult;
import com.fintex.ce.util.FilterUtils;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static com.fintex.ce.domain.model.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.SCALED;
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
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final var req = mock(AverageManagementExpenseCalculation.class);

    doCallRealMethod().when(sut).setFeeValues(any(), any());
    // ACT

    sut.setFeeValues(req, TEN);

    // VERIFY
    verify(req).setInitialFee(TEN);
    verify(req).setModifiedFee(TEN);
  }

  @Test
  void shouldGetScaledAverageMer_whenCallsGetAbsoluteAndForceReportFeeHoldingList() {
    // SETUP
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    doCallRealMethod().when(sut).getScaledAverageMer(anyMap());

    // ACT
    sut.getScaledAverageMer(holdings);

    // VERIFY
    verify(sut).getAbsoluteAndForceReportFeeHoldingList(holdings);
  }

  @Test
  void shouldGetScaledAverageMer_whenWhenParameterTypeAbsoluteCallsGetAverageMerByParameterTypeWithAllFinancialInstrumentTypes() {
    // SETUP
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> absoluteHoldings = Stream.of(holdings.get(
        FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA), holdings.get(
            FinancialInstrumentType.ETF_US),
        holdings.get(FinancialInstrumentType.ETF_CANADA), holdings.get(FinancialInstrumentType.STOCK_US), holdings.get(
            FinancialInstrumentType.STOCK_CANADA), holdings.get(FinancialInstrumentType.CASH))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    doCallRealMethod().when(sut).getAbsoluteAverageMer(anyMap());

    // ACT
    sut.getAbsoluteAverageMer(holdings);

    // VERIFY
    verify(sut).getAverageMerByParameterType(absoluteHoldings);
  }

  @Test
  void shouldGetAbsoluteAndForceReportFeeHoldingList_whenIsPappedProperly() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(sut).getAbsoluteAndForceReportFeeHoldingList(anyMap());
    final List<AverageManagementExpenseCalculation> absoluteOrForceReportFeeHoldings = Stream.of(holdings.get(
        FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA), holdings.get(
            FinancialInstrumentType.ETF_US), holdings.get(FinancialInstrumentType.ETF_CANADA))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    // ACT
    final List<AverageManagementExpenseCalculation> result = sut.getAbsoluteAndForceReportFeeHoldingList(holdings);

    // VERIFY
    assertEquals(absoluteOrForceReportFeeHoldings, result);
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculateAverageManagementExpenseRatio() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDTOList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(sut).getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // ACT
    sut.getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // VERIFY
    verify(sut, times(2)).calculateAverageManagementExpenseRatio(any(), any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculateMarketValueQualified() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDTOList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(sut).getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // ACT
    sut.getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // VERIFY
    verify(sut, times(2)).calculateMarketValueQualified(any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsCalculatePercentageQualified() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDTOList = List.of(
        new AverageManagementExpenseCalculation(), new AverageManagementExpenseCalculation());
    doCallRealMethod().when(sut).getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // ACT
    sut.getAverageMerByParameterType(averageManagementExpenseCalculationDTOList);

    // VERIFY
    verify(sut, times(2)).calculatePercentageQualified(any(), any());
  }

  @Test
  void shouldGetAverageMerByParameterType_whenCallsGetAmountOfMarketValueQualified() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    doCallRealMethod().when(sut).getAverageMerByParameterType(anyList());

    // ACT
    sut.getAverageMerByParameterType(anyList());

    // VERIFY
    verify(sut).getAmountOfMarketValues(anyList());
  }

  @Test
  void shouldGetScaledAverageMer_whenWhenParameterTypeScaledCallsGetAverageMerByParameterTypeWithCanadaMutualFundsUsEtfAndCanadaEtfHoldings() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);

    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(sut).getScaledAverageMer(anyMap());
    doCallRealMethod().when(sut).getAbsoluteAndForceReportFeeHoldingList(anyMap());

    // ACT
    sut.getScaledAverageMer(holdings);

    // VERIFY
    verify(sut).getAverageMerByParameterType(
        Stream.of(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
            holdings.get(FinancialInstrumentType.ETF_US), holdings.get(FinancialInstrumentType.ETF_CANADA))
            .map(Map::values).flatMap(Collection::stream)
            .collect(Collectors.toList()));
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenWhenParameterTypeForceReportFeeCallsGetAbsoluteAndForceReportFeeHoldingList() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);

    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(sut).getForceReportFeeAverageMer(anyMap());

    // ACT
    sut.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(sut).getAbsoluteAndForceReportFeeHoldingList(holdings);
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenWhenParameterTypeForceReportFeeCallsGetAverageMerByParameterTypeWithCanadaMutualFundsUsEtfAndCanadaEtfHoldings() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(sut).getForceReportFeeAverageMer(anyMap());
    doCallRealMethod().when(sut).getAbsoluteAndForceReportFeeHoldingList(anyMap());

    // ACT
    sut.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(sut).getAverageMerByParameterType(Stream.of(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA), holdings.get(
        FinancialInstrumentType.SEGREGATED_FUND_CANADA), holdings.get(FinancialInstrumentType.ETF_US), holdings.get(FinancialInstrumentType.ETF_CANADA))
        .map(Map::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toList()));
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenCallsIsMerPresentForAHolding4Times() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    doCallRealMethod().when(sut).getForceReportFeeAverageMer(anyMap());

    // ACT
    sut.getForceReportFeeAverageMer(holdings);

    // VERIFY
    verify(sut, times(6)).isMerPresentForHolding(any(), any());
  }

  @Test
  void shouldGetForceReportFeeAverageMer_whenReturnsNullWhenMerValueIsNull() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> holdings = getAverageMerCalculationDtoMap();
    final Function<AverageManagementExpenseCalculation, BigDecimal> expenseRatioFunction = AverageManagementExpenseCalculation::getManagementExpenseRatio;
    when(sut.isMerPresentForHolding(holdings.get(FinancialInstrumentType.MUTUAL_FUND_CANADA), expenseRatioFunction)).thenReturn(
        true);

    doCallRealMethod().when(sut).getForceReportFeeAverageMer(anyMap());
    // ACT
    final BigDecimal forceReportFeeAverageMer = sut.getForceReportFeeAverageMer(holdings);

    // VERIFY
    assertNull(forceReportFeeAverageMer);
  }

  @Test
  void shouldIsMerPresentForAHolding_whenReturnsTrueIfMerIsAbsent() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);

    final Map<Holding, AverageManagementExpenseCalculation> mockAverageMerCalculationDtoList = Map.of(mock(
        Holding.class), new AverageManagementExpenseCalculation());
    final Function<AverageManagementExpenseCalculation, BigDecimal> functionMock = mock(Function.class);
    doCallRealMethod().when(sut).isMerPresentForHolding(any(), any());

    // ACT
    final boolean presentForAHolding = sut.isMerPresentForHolding(mockAverageMerCalculationDtoList, functionMock);

    // VERIFY
    assertTrue(presentForAHolding);
  }

  @Test
  void shouldIsMerPresentForAHolding_whenReturnsFalseIfMerIsPresent() {
    // SETUP
    final var sut = mock(MERCalculationServiceImpl.class);

    final AverageManagementExpenseCalculation mockAverageManagementExpenseCalculationDto = mock(
        AverageManagementExpenseCalculation.class);
    when(mockAverageManagementExpenseCalculationDto.getManagementExpenseRatio()).thenReturn(BigDecimal.ZERO);
    final Map<Holding, AverageManagementExpenseCalculation> mockAverageMerCalculationDtoList = Map.of(mock(
        Holding.class), mockAverageManagementExpenseCalculationDto);

    doCallRealMethod().when(sut).isMerPresentForHolding(any(), any());
    // ACT
    final boolean presentForAHolding = sut.isMerPresentForHolding(
        mockAverageMerCalculationDtoList, AverageManagementExpenseCalculation::getManagementExpenseRatio);

    // VERIFY
    assertFalse(presentForAHolding);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperly() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setModifiedFee(new BigDecimal("0.97"));
    averageManagementExpenseCalculationDTO.setPercentageQualified(new BigDecimal("34"));
    doCallRealMethod().when(sut).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = sut.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculationDTO);

    // VERIFY
    assertEquals(new BigDecimal("62.98"), result);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperlyWhenModifiedFeeIsNull() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setPercentageQualified(new BigDecimal("34"));
    doCallRealMethod().when(sut).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = sut.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculationDTO);

    // VERIFY
    assertEquals(new BigDecimal("30"), result);
  }

  @Test
  void shouldCalculateAverageManagementExpenseRatio_whenAverageManagementExpenseRatioValueIsCalculatedProperlyWhenPercentageQualifiedIsNull() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setModifiedFee(new BigDecimal("0.97"));
    doCallRealMethod().when(sut).calculateAverageManagementExpenseRatio(any(), any());

    // ACT
    final BigDecimal result = sut.calculateAverageManagementExpenseRatio(new BigDecimal("30"),
        averageManagementExpenseCalculationDTO);

    // VERIFY
    assertEquals(new BigDecimal("30"), result);
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDTOisMappedProperly() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setMarketValueQualified(new BigDecimal("50000"));
    doCallRealMethod().when(sut).calculatePercentageQualified(any(), any());

    // ACT
    sut.calculatePercentageQualified(averageManagementExpenseCalculationDTO, new BigDecimal("70000"));

    // VERIFY
    assertEquals(new BigDecimal("0.714285714285714"), averageManagementExpenseCalculationDTO.getPercentageQualified());
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDTOisMappedProperlyWhenMarketValueQualifiedIsNull() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();

    // ACT
    sut.calculatePercentageQualified(averageManagementExpenseCalculationDTO, new BigDecimal("70000"));

    // VERIFY
    assertNull(averageManagementExpenseCalculationDTO.getPercentageQualified());
  }

  @Test
  void shouldCalculatePercentageQualified_whenAverageMerCalculationDTOisMappedProperlyWhenMarketValueQualifiedIsZero() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setMarketValueQualified(BigDecimal.ZERO);

    // ACT
    sut.calculatePercentageQualified(averageManagementExpenseCalculationDTO, new BigDecimal("70000"));

    // VERIFY
    assertNull(averageManagementExpenseCalculationDTO.getPercentageQualified());
  }

  @Test
  void shouldCalculateMarketValueQualified_whenIsMappedProperlyWhenModifiedFeeValueExists() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO.setModifiedFee(new BigDecimal("100"));
    averageManagementExpenseCalculationDTO.setMarketValue(new BigDecimal("10000"));
    doCallRealMethod().when(sut).calculateMarketValueQualified(averageManagementExpenseCalculationDTO);

    // ACT
    sut.calculateMarketValueQualified(averageManagementExpenseCalculationDTO);

    // VERIFY
    assertEquals(averageManagementExpenseCalculationDTO.getMarketValue(), averageManagementExpenseCalculationDTO
        .getMarketValueQualified());
  }

  @Test
  void shouldCalculateMarketValueQualified_whenIsMappedProperlyWhenModifiedFeeValueIsNull() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO = new AverageManagementExpenseCalculation();
    doCallRealMethod().when(sut).calculateMarketValueQualified(averageManagementExpenseCalculationDTO);

    // ACT
    sut.calculateMarketValueQualified(averageManagementExpenseCalculationDTO);

    // VERIFY
    assertEquals(new BigDecimal(BigInteger.ZERO), averageManagementExpenseCalculationDTO.getMarketValueQualified());
  }

  @Test
  void shouldGetAmountOfMarketValueQualified_whenReturnsCorrectValue() {
    // SETUP
    final var sut = mock(AverageManagementExpenseCalculationService.class);
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO1 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO1.setMarketValue(new BigDecimal("10"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO2 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO2.setMarketValue(new BigDecimal("20"));
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO3 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO3.setMarketValue(new BigDecimal("30"));
    final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDTOList = List.of(
        averageManagementExpenseCalculationDTO1, averageManagementExpenseCalculationDTO2,
        averageManagementExpenseCalculationDTO3);
    doCallRealMethod().when(sut).getAmountOfMarketValues(any());

    // ACT
    final BigDecimal amountOfMarketValueQualified = sut.getAmountOfMarketValues(
        averageManagementExpenseCalculationDTOList);

    // VERIFY
    assertEquals(new BigDecimal("60"), amountOfMarketValueQualified);
  }

  @Test
  void shouldSetNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds_whenCheckResult2() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP

      final var sut = mock(AverageManagementExpenseCalculationService.class);

      final var reqDTO = mock(AverageMerCommand.class);
      final var resDTO = new AverageMerResult();
      resDTO.getManagementExpenseRatio().put(ABSOLUTE, ONE);
      resDTO.getManagementExpenseRatio().put(SCALED, TEN);
      resDTO.getManagementExpenseRatio().put(FORCE_REPORT_FEE, ZERO);
      final var holding1 = new Holding();
      final var holding2 = new Holding();
      holding1.setHoldingType(FinancialInstrumentType.STOCK_US);
      holding2.setHoldingType(FinancialInstrumentType.CASH);
      final var holdings = List.of(holding1, holding2);
      final var expected = new HashMap<ParameterType, BigDecimal>();
      expected.put(ABSOLUTE, ONE);
      expected.put(SCALED, null);
      expected.put(FORCE_REPORT_FEE, null);

      when(reqDTO.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(anyMap(), any());
      doCallRealMethod().when(sut).setNullForScaledIfHoldingContainsNoFunds(any(), any());
      doCallRealMethod().when(sut).setNullForForcedReportFeeIfHoldingContainsNoFunds(any(), any());
      // ACT
      sut.setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(resDTO.getManagementExpenseRatio(), reqDTO);

      // VERIFY
      Assertions.assertNotNull(resDTO);
      ComparisonUtils.compareMaps(expected, resDTO.getManagementExpenseRatio());
    }
  }

  private Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> getAverageMerCalculationDtoMap() {
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
    final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO7 = new AverageManagementExpenseCalculation();
    averageManagementExpenseCalculationDTO7.setMarketValue(new BigDecimal("70"));

    return Map.of(FinancialInstrumentType.MUTUAL_FUND_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO1),
        FinancialInstrumentType.ETF_US, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO2),
        FinancialInstrumentType.ETF_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO3),
        FinancialInstrumentType.STOCK_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO4),
        FinancialInstrumentType.STOCK_US, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO5),
        FinancialInstrumentType.CASH, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO6),
        FinancialInstrumentType.SEGREGATED_FUND_CANADA, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO7));
  }

}