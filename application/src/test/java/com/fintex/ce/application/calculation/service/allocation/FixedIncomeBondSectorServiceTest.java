package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
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

  private FixedIncomeBondSectorService mockService(FixedIncomeSectorResponseMapper responseMapper) {
    return mock(FixedIncomeBondSectorService.class, withSettings()
        .useConstructor(responseMapper));
  }

  @Test
  void shouldFetch_whenCheckResult() {
    var service = mockService(mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    var rawData = new FixedIncomeBondSector();
    rawData.setFixedIncomeBondSectors(Map.of(FixedIncomeSectorAllocationType.CORPORATE_BONDS, TEN));
    var data = Map.of(holding, rawData);

    doCallRealMethod().when(service).fetchExposures(any(), any());
    var result = service.fetchExposures(command, data);

    assertTrue(result.allocations().containsKey(holding));
    assertEquals(TEN, result.allocations().get(holding).get(FixedIncomeSectorAllocationType.CORPORATE_BONDS));
    assertTrue(result.warnings().isEmpty());
  }

  @Test
  void shouldEmitWarning_whenSecurityIsUnknown() {
    var service = mockService(mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    when(holding.getIdsString()).thenReturn("HOLDING-1");
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));

    doCallRealMethod().when(service).getMetric();
    doCallRealMethod().when(service).fetchExposures(any(), any());
    var result = service.fetchExposures(command, Map.of());

    assertEquals(1, result.warnings().size());
    var warning = result.warnings().getFirst();
    assertEquals(SECURITY_NOT_FOUND_FOR_METRIC.getCode(), warning.getCode());
    assertEquals("HOLDING-1", warning.getMetadata().get("holdingId"));
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0, result.allocations().get(holding).get(FixedIncomeSectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldEmitWarning_whenDataIsMissing() {
    var service = mockService(mock(FixedIncomeSectorResponseMapper.class));

    var holding = mock(PortfolioHolding.class);
    when(holding.getIdsString()).thenReturn("HOLDING-1");
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    var data = Map.of(holding, new FixedIncomeBondSector());

    doCallRealMethod().when(service).fetchExposures(any(), any());
    var result = service.fetchExposures(command, data);

    assertEquals(1, result.warnings().size());
    var warning = result.warnings().getFirst();
    assertEquals(MISSING_FIXED_INCOME_BOND_SECTOR.getCode(), warning.getCode());
    assertEquals("HOLDING-1", warning.getMetadata().get("holdingId"));
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0, result.allocations().get(holding).get(FixedIncomeSectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsEmptyMap() {
    var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
    var service = mockService(responseMapper);

    var expected = mock(FixedIncomeSectorResult.class);
    when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

    doCallRealMethod().when(service).calculate(any(), any());
    FixedIncomeSectorResult actual = service.calculate(
        new ExposureDataHolder<>(Map.of(), List.of()), List.of());

    assertEquals(expected, actual);
    verify(responseMapper).toEmptyResponse(List.of());
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
      var service = mockService(responseMapper);

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(FixedIncomeSectorAllocationType.CORPORATE_BONDS, TEN));
      var expected = mock(FixedIncomeSectorResult.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(responseMapper.fromNetProducts(any(), any())).thenReturn(expected);
      doCallRealMethod().when(service).calculate(any(), any());
      var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, FixedIncomeSectorAllocationType.values());
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyNumericOutputWithMultiHoldingPortfolio() {
    var service = new FixedIncomeBondSectorService(new FixedIncomeSectorResponseMapper());

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
            FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
            FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.5")),
        rbf605, Map.of(
            FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.7"),
            FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.1")));

    var result = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of(aom, rbf605));

    var sectors = result.getFixedIncomeSector();
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS)
        .compareTo(new BigDecimal("0.525")));
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.CORPORATE_BONDS)
        .compareTo(new BigDecimal("0.475")));
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(FixedIncomeSectorResponseMapper.class);
      var service = mockService(responseMapper);

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(FixedIncomeSectorAllocationType.CORPORATE_BONDS, TEN));
      var netProducts = mock(Map.class);
      var expected = mock(FixedIncomeSectorResult.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, FixedIncomeSectorAllocationType.values()))
          .thenReturn(netProducts);
      when(responseMapper.fromNetProducts(netProducts, List.of())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(netProducts, List.of());
      assertEquals(expected, actual);
    }
  }
}
