package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquityCountryExposureResult;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityCountryExposureCalculationServiceImplTest {

  @SuppressWarnings("unchecked")
  private final SecurityDataPort<EquityCountryAllocation> securityDataPort = mock(SecurityDataPort.class);
  private final CountryAllocationMappingService countryAllocationMappingService = mock(CountryAllocationMappingService.class);

  @Test
  void shouldPerform_whenVerifyValidateHoldings() {
    final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
        withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

    final PortfolioHoldingsCommand req = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(mock(Holding.class));
    when(req.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(req);
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult() {
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType canada = CountryRegionType.CANADA;

    final Holding h1 = new Holding().setValue(BigDecimal.ONE).setType(HoldingType.CASH);
    final Holding h2 = new Holding().setValue(BigDecimal.TEN).setType(HoldingType.US_ETF);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(canada, BigDecimal.valueOf(2), CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(21)),
        h2, Map.of(canada, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    final BigDecimal actual = e.calculateNetProduct(canada, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    assertEquals(0, BigDecimal.valueOf(26).compareTo(actual));
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult2() {
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType type = CountryRegionType.EMERGING_MARKET;

    final Holding h1 = new Holding().setValue(BigDecimal.ONE).setType(HoldingType.US_ETF);
    final Holding h2 = new Holding().setValue(BigDecimal.TEN).setType(HoldingType.CANADA_MUTUAL_FUNDS);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(type, BigDecimal.valueOf(2), CountryRegionType.CANADA, BigDecimal.valueOf(21)),
        h2, Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    final BigDecimal actual = e.calculateNetProduct(type, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    assertEquals(0, BigDecimal.valueOf(6).compareTo(actual));
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, List.of(), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

      final var exposures = mock(Map.class);
      final var expected = new EquityCountryExposureResult();
      expected.setEquityCountryExposure(EquityCountryExposureCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final var actual = sut.calculate(exposures, List.of(), List.of());

      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldFetchExposures_whenCheckResult() {
    final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
        withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

    final var holding = mock(Holding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of());

    final var allocation = new EquityCountryAllocation()
        .setAllocations(Map.of("CAN", BigDecimal.valueOf(0.65)));
    when(securityDataPort.fetch(any(), any())).thenReturn(Map.of(holding, allocation));

    final var expected = Map.of(holding, Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(0.65)));
    when(countryAllocationMappingService.mapToCountryRegions(any(), anyList(), any())).thenReturn(expected);

    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(command, List.of());

    assertEquals(expected, actual);
    assertTrue(actual.containsKey(holding));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      verify(sut).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}