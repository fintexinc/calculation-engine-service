package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

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
class EquityStyleboxExposureCalculationServiceImplTest {

  @Test
  void shouldFetch_whenCheckResult() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
    final var service = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var exposure = new EquityStyleboxExposure();
    exposure.setBoxValues(Map.of(StyleBoxType.LARGE_VALUE, BigDecimal.TEN));
    final var rawData = Map.of(holding, exposure);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
    final var service = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(StyleBoxType.LARGE_VALUE, BigDecimal.TEN));

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    verify(service).calculateNetProducts(exposures, holdings, StyleBoxType.values());
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
    final var service = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(StyleBoxType.LARGE_VALUE, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    when(service.calculateNetProducts(exposures, holdings, StyleBoxType.values())).thenReturn(netProducts);

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
      final var service = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));
      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
      final var service = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new EquityStyleboxExposureResult();
      expected.setEquityStyleboxExposure(Map.of());
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      Assertions.assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(any());
    }
  }
}
