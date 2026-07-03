package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class FixedIncomeBondSectorServiceTest {

  private FixedIncomeBondSectorService mockService(
      SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeFetcher,
      FixedIncomeSectorResponseMapper responseMapper) {
    return mock(FixedIncomeBondSectorService.class, withSettings()
        .useConstructor(fixedIncomeFetcher, responseMapper));
  }

  @Test
  void shouldFetch_whenCheckResult() {
    var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    var service = mockService(fixedIncomeFetcher, mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));
    var rawData = new FixedIncomeBondSecurities();
    rawData.setFixedIncomeBondSectors(Map.of(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, TEN));
    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawData));

    doCallRealMethod().when(service).fetchExposures(any());
    var result = service.fetchExposures(command);

    assertTrue(result.allocations().containsKey(holding));
    assertEquals(TEN, result.allocations().get(holding).get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS));
    assertTrue(result.warnings().isEmpty());
    verify(fixedIncomeFetcher).fetch(List.of(holding), List.of(DataProvider.MORNINGSTAR));
  }

  @Test
  void shouldEmitWarning_whenSecurityIsUnknown() {
    var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    var service = mockService(fixedIncomeFetcher, mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    when(holding.getIdsString()).thenReturn("HOLDING-1");
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));
    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of());

    doCallRealMethod().when(service).fetchExposures(any());
    var result = service.fetchExposures(command);

    assertEquals(1, result.warnings().size());
    var warning = result.warnings().getFirst();
    assertEquals(MISSING_FIXED_INCOME_BOND_SECTOR.getCode(), warning.getCode());
    assertEquals(MISSING_FIXED_INCOME_BOND_SECTOR.getFormattedMessage("HOLDING-1"), warning.getMessage());
    assertEquals("HOLDING-1", warning.getMetadata().get("holdingId"));
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0, result.allocations().get(holding).get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS)
        .compareTo(ZERO));
  }

  @Test
  void shouldEmitWarning_whenSecurityDataIsMissing() {
    var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    var service = mockService(fixedIncomeFetcher, mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    when(holding.getIdsString()).thenReturn("HOLDING-1");
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));
    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of(holding, new FixedIncomeBondSecurities()));

    doCallRealMethod().when(service).fetchExposures(any());
    var result = service.fetchExposures(command);

    assertEquals(1, result.warnings().size());
    var warning = result.warnings().getFirst();
    assertEquals(MISSING_FIXED_INCOME_BOND_SECTOR.getCode(), warning.getCode());
    assertEquals(MISSING_FIXED_INCOME_BOND_SECTOR.getFormattedMessage("HOLDING-1"), warning.getMessage());
    assertEquals("HOLDING-1", warning.getMetadata().get("holdingId"));
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0, result.allocations().get(holding).get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS)
        .compareTo(ZERO));
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var service = mockService(mock(SecurityDataFetcher.class), mock(FixedIncomeSectorResponseMapper.class));

      var exposures = mock(Map.class);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<FixedIncomeSecuritiesAllocationType>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
      var service = mockService(mock(SecurityDataFetcher.class), responseMapper);

      var expected = mock(FixedIncomeSectorResult.class);
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      FixedIncomeSectorResult actual = service.calculate(
          new ExposureDataHolder<>(Map.of(), List.of()), List.of());

      assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(List.of());
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
      var service = mockService(mock(SecurityDataFetcher.class), responseMapper);

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, TEN));
      var expected = mock(FixedIncomeSectorResult.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(responseMapper.fromNetProducts(any(), any())).thenReturn(expected);
      doCallRealMethod().when(service).calculate(any(), any());
      var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, FixedIncomeSecuritiesAllocationType.values());
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyNumericOutputWithMultiHoldingPortfolio() {
    var service = new FixedIncomeBondSectorService(
        mock(SecurityDataFetcher.class), new FixedIncomeSectorResponseMapper());

    var aom = mock(PortfolioHolding.class);
    when(aom.getValue()).thenReturn(BigDecimal.valueOf(70));
    var rbf605 = mock(PortfolioHolding.class);
    when(rbf605.getValue()).thenReturn(BigDecimal.valueOf(30));

    // weight(aom)=0.7, weight(rbf605)=0.3
    // netProduct(GOVERNMENT_BONDS) = 0.7*0.3 + 0.3*0.7 = 0.42
    // netProduct(CORPORATE_BONDS) = 0.7*0.5 + 0.3*0.1 = 0.38 → sum=0.80
    // after reScaleAbs: GOVERNMENT_BONDS=0.525, CORPORATE_BONDS=0.475
    var exposures = Map.of(
        aom, Map.of(
            FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
            FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, new BigDecimal("0.5")),
        rbf605, Map.of(
            FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.7"),
            FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, new BigDecimal("0.1")));

    var result = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of(aom, rbf605));

    var sectors = result.getFixedIncomeSector();
    assertEquals(0, sectors.get(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS)
        .compareTo(new BigDecimal("0.525")));
    assertEquals(0, sectors.get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS)
        .compareTo(new BigDecimal("0.475")));
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
      var service = mockService(mock(SecurityDataFetcher.class), responseMapper);

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, TEN));
      var netProducts = mock(Map.class);
      var expected = mock(FixedIncomeSectorResult.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, FixedIncomeSecuritiesAllocationType.values()))
          .thenReturn(netProducts);
      when(responseMapper.fromNetProducts(netProducts, List.of())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(netProducts, List.of());
      assertEquals(expected, actual);
    }
  }
}
