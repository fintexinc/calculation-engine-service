package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class FixedIncomeStyleboxExposureServiceTest {

  @Test
  void shouldFetch_whenCheckResult() {
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var service = mock(FixedIncomeStyleboxExposureService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    final var exposure = new FixedIncomeStyleboxExposure();
    exposure.setBoxValues(Map.of(FixedIncomeStyleBoxType.HIGH_EXTENSIVE, BigDecimal.TEN));
    final var data = Map.of(holding, exposure);

    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(command, data);
    final var actual = result.allocations();

    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var service = mock(FixedIncomeStyleboxExposureService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(FixedIncomeStyleBoxType.HIGH_EXTENSIVE, BigDecimal.TEN));

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    verify(service).calculateNetProducts(exposures, holdings, FixedIncomeStyleBoxType.values());
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var service = mock(FixedIncomeStyleboxExposureService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(FixedIncomeStyleBoxType.HIGH_EXTENSIVE, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    when(service.calculateNetProducts(exposures, holdings, FixedIncomeStyleBoxType.values())).thenReturn(netProducts);

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
      final var service = mock(FixedIncomeStyleboxExposureService.class, withSettings()
          .useConstructor(responseMapper));
      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
      final var service = mock(FixedIncomeStyleboxExposureService.class, withSettings()
          .useConstructor(responseMapper));

      final var exposures = mock(Map.class);
      final var expected = FixedIncomeStyleboxExposureResult.builder()
          .fixedIncomeStyleboxExposure(Map.of())
          .warnings(List.of())
          .build();
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      Assertions.assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(any());
    }
  }
}
