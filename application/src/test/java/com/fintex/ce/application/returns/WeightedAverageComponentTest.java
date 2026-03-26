package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.metric.formula.SumProduct;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class WeightedAverageComponentTest {

  @Test
  void shouldCollectMonthlyWeightEntries_whenCheckResult() {
    // SETUP
    final var sut = mock(WeightedAverageComponent.class);
    final var holding = mock(Holding.class);
    final var date = LocalDate.of(2020, 10, 10);
    final var oldValue = new BigDecimal("2.2");
    final var expectedNewValue = new BigDecimal("1.1");

    final var map = Map.of(holding, expectedNewValue);

    doCallRealMethod().when(sut).collectMonthlyWeightEntries(any());

    // ACT
    final Function<Map.Entry<Holding, TreeMap<LocalDate, BigDecimal>>, TreeMap<LocalDate, BigDecimal>> actualFunction = sut
        .collectMonthlyWeightEntries(map);

    final Map<LocalDate, BigDecimal> actual = actualFunction.apply(Map.entry(holding, new TreeMap<>(Map.of(date,
        oldValue))));

    // VERIFY
    assertEquals(expectedNewValue, actual.get(date));
  }

  @Test
  void shouldCalculateEndingPortfolioWeight_whenVefiryCalculateInitialPortfolioWeight() {
    try (var portfolioUtilsMock = mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(WeightedAverageComponent.class);

      final var holding = mock(Holding.class);
      final var pBaseTotalReturn = Map.of(holding, new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE)));

      when(sut.collectMonthlyWeightEntries(anyMap())).thenReturn(i -> i.getValue());

      doCallRealMethod().when(sut).calculateEndingPortfolioWeight(anyMap());

      // ACT
      sut.calculateEndingPortfolioWeight(pBaseTotalReturn);

      // VERIFY
      portfolioUtilsMock.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(eq(Set.of(holding))));
    }
  }

  @Test
  void shouldCalculateEndingPortfolioWeight_whenVerifyCollectMonthlyWeightEntries() {
    try (var portfolioUtilsMock = mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(WeightedAverageComponent.class);

      final var holding = mock(Holding.class);
      var map = mock(TreeMap.class);
      portfolioUtilsMock.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(anyCollection())).thenReturn(map);
      when(sut.collectMonthlyWeightEntries(anyMap())).thenReturn(i -> i.getValue());

      doCallRealMethod().when(sut).calculateEndingPortfolioWeight(anyMap());

      // ACT
      sut.calculateEndingPortfolioWeight(Map.of());

      // VERIFY
      verify(sut).collectMonthlyWeightEntries(same(map));
    }
  }

  @Test
  void shouldCalculateTotalPortfolioReturnFactor_whenVerifyCalculate() {
    // SETUP
    try (var sumProductMockedConstruction = mockConstruction(SumProduct.class,
        (sumProductMock, setting) -> {
          when(sumProductMock.setMap2KeyFinder(any())).thenReturn(sumProductMock);
          when(sumProductMock.calculate()).thenReturn(new TreeMap());
        })) {
      final var sut = mock(WeightedAverageComponent.class,
          withSettings().useConstructor(ReturnFactorScale.AS_IS));

      final var holding = mock(Holding.class);
      when(sut.collectMonthlyWeightEntries(anyMap())).thenReturn(i -> i.getValue());

      doCallRealMethod().when(sut).calculateTotalPortfolioReturnFactor(anyMap(), anyMap());

      // ACT
      sut.calculateTotalPortfolioReturnFactor(Map.of(), Map.of());

      // VERIFY
      assertEquals(1, sumProductMockedConstruction.constructed().size());
      final var sumProduct = sumProductMockedConstruction.constructed().get(0);
      verify(sumProduct).calculate();
    }
  }

  @Test
  void shouldCalculateTotalPortfolioReturnFactor_whenCheckResult() {
    // SETUP
    final var map = new TreeMap<>(Map.of(LOCAL_DATE_NOW, new BigDecimal(20)));

    try (var sumProductMockedConstruction = mockConstruction(SumProduct.class,
        (sumProductMock, setting) -> {
          when(sumProductMock.setMap2KeyFinder(any())).thenReturn(sumProductMock);
          when(sumProductMock.calculate()).thenReturn(map);
        })) {

      final var sut = mock(WeightedAverageComponent.class,
          withSettings().useConstructor(ReturnFactorScale.SCALE_OF_TWO));

      final var holding = mock(Holding.class);
      when(sut.collectMonthlyWeightEntries(anyMap())).thenReturn(i -> i.getValue());

      doCallRealMethod().when(sut).calculateTotalPortfolioReturnFactor(anyMap(), anyMap());

      // ACT
      final var actual = sut.calculateTotalPortfolioReturnFactor(Map.of(), Map.of());

      // VERIFY
      Assertions.assertNotNull(actual);
      ComparisonUtils.compareMaps(Map.of(LOCAL_DATE_NOW, new BigDecimal("1.2")), actual);
    }
  }

  @Test
  void shouldCalculateWeightedAverage_whenVerifyCalculateEndingPortfolioWeight() {
    // SETUP
    final var sut = mock(WeightedAverageComponent.class);
    final var returns = mock(Map.class);

    doCallRealMethod().when(sut).calculateWeightedAverage(anyMap());

    // ACT
    sut.calculateWeightedAverage(returns);

    // VERIFY
    verify(sut).calculateEndingPortfolioWeight(returns);
  }

  @Test
  void shouldCalculateWeightedAverage_whenVerifyCalculateTotalPortfolioReturnFacto() {
    // SETUP
    final var sut = mock(WeightedAverageComponent.class);
    final var returns = mock(Map.class);
    final var endingPortfolioWeight = mock(Map.class);

    when(sut.calculateEndingPortfolioWeight(anyMap())).thenReturn(endingPortfolioWeight);

    doCallRealMethod().when(sut).calculateWeightedAverage(anyMap());

    // ACT
    sut.calculateWeightedAverage(returns);

    // VERIFY
    verify(sut).calculateTotalPortfolioReturnFactor(returns, endingPortfolioWeight);
  }
}