package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquityCountryExposureResult;
import com.fintex.ce.port.output.cache.EquityCountryAllocationCachePort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityCountryExposureCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyValidateHoldings() {
    // SETUP
    final var storage = mock(EquityCountryAllocationCachePort.class);
    final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
        withSettings().useConstructor(storage));

    final PortfolioHoldingsCommand req = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(mock(Holding.class));
    when(req.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(req);

    // VERIFY
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult() {
    // SETUP
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType canada = CountryRegionType.CANADA;

    final Holding h1 = new Holding().setValue(BigDecimal.ONE).setType(HoldingType.CASH);
    final Holding h2 = new Holding().setValue(BigDecimal.TEN).setType(HoldingType.US_ETF);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(canada, BigDecimal.valueOf(2), CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(21)),
        h2, Map.of(canada, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    // ACT
    final BigDecimal actual = e.calculateNetProduct(canada, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(26).compareTo(actual));
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult2() {
    // SETUP
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType type = CountryRegionType.EMERGING_MARKET;

    final Holding h1 = new Holding().setValue(BigDecimal.ONE).setType(HoldingType.US_ETF);
    final Holding h2 = new Holding().setValue(BigDecimal.TEN).setType(HoldingType.CANADA_MUTUAL_FUNDS);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(type, BigDecimal.valueOf(2), CountryRegionType.CANADA, BigDecimal.valueOf(21)),
        h2, Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    // ACT
    final BigDecimal actual = e.calculateNetProduct(type, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(6).compareTo(actual));
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(EquityCountryAllocationCachePort.class);
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(storage));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(EquityCountryAllocationCachePort.class);
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(storage));

      final var exposures = mock(Map.class);
      final var expected = new EquityCountryExposureResult();
      expected.setEquityCountryExposure(EquityCountryExposureCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(EquityCountryAllocationCachePort.class);
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(storage));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      when(storage.load(any(), any(), any(), any())).thenReturn(exposures);
      doCallRealMethod().when(sut).fetchExposures(any(), any());
      // ACT
      final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

      // VERIFY
      assertEquals(exposures, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}