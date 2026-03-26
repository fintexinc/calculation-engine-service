package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.EquityStyleboxExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityStyleboxExposureResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
    final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var exposure = new EquityStyleboxExposure();
    exposure.setBoxValues(Map.of("LARGE_VALUE", BigDecimal.TEN));
    final var rawData = Map.of(holding, exposure);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), new java.util.ArrayList<>());

    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts(){
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
    final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN));

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    sut.calculate(exposures, holdings, List.of());

    verify(sut).calculateNetProducts(exposures, holdings, EquityStyleboxType.values());
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
    final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    when(sut.calculateNetProducts(exposures, holdings, EquityStyleboxType.values())).thenReturn(netProducts);

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    sut.calculate(exposures, holdings, List.of());

    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
      final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));
      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, List.of(), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquityStyleboxExposureResponseMapper.class);
      final var sut = mock(EquityStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new EquityStyleboxExposureResult();
      expected.setEquityStyleboxExposure(Map.of());
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final var actual = sut.calculate(exposures, List.of(), List.of());

      Assertions.assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(any());
    }
  }
}
