package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.MaturityAllocationResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.error.Notification;

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
class MaturityAllocationServiceTest {

  @Test
  void fetchExposures_checkResult() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var service = mock(MaturityAllocationService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var maturityAllocation = new MaturityAllocation();
    maturityAllocation.setMaturityDurationValues(Map.of("FIVE_TO_SEVEN_YEARS", BigDecimal.TEN));
    final var rawData = Map.of(holding, maturityAllocation);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var service = mock(MaturityAllocationService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    verify(service).calculateNetProducts(exposures, holdings, MaturityAllocationType.values());
  }

  @Test
  void calculate_verifyFromNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var service = mock(MaturityAllocationService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    final List<Notification> warnings = List.of();
    when(service.calculateNetProducts(exposures, holdings, MaturityAllocationType.values())).thenReturn(netProducts);

    doCallRealMethod().when(service).calculate(any(), any());
    service.calculate(new ExposureDataHolder<>(exposures, warnings), holdings);

    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var service = mock(MaturityAllocationService.class, withSettings()
        .useConstructor(fetcher, responseMapper));
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
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
      final var responseMapper = mock(MaturityAllocationResponseMapper.class);
      final var service = mock(MaturityAllocationService.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = MaturityAllocationResult.builder()
          .maturityAllocation(MaturityAllocationService.ALLOCATION_DEFAULT_MAP)
          .warnings(List.of())
          .build();
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      Assertions.assertEquals(expected, actual);
    }
  }
}
