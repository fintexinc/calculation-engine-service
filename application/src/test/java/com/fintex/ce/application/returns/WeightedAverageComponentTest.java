package com.fintex.ce.application.returns;

import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedAverageComponentTest {

  private static final LocalDate DECEMBER = LocalDate.parse("2023-12-31");
  private static final LocalDate JANUARY = LocalDate.parse("2024-01-31");
  private static final LocalDate FEBRUARY = LocalDate.parse("2024-02-29");

  private final WeightedAverageComponent component = new WeightedAverageComponent();

  @Test
  void shouldCalculateValueWeightedReturns_whenHoldingsHaveDifferentValuesAndReturns() {
    PortfolioHolding firstHolding = holding("FIRST", "100");
    PortfolioHolding secondHolding = holding("SECOND", "300");
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(
        firstHolding, returns("10", "20"),
        secondHolding, returns("30", "40"));

    NavigableMap<LocalDate, BigDecimal> result = component.calculateWeightedAverage(returns, ReturnFactorScale.AS_IS);

    assertThat(result).hasSize(2);
    assertThat(result.get(JANUARY)).isEqualByComparingTo("25");
    assertThat(result.get(FEBRUARY)).isEqualByComparingTo("35");
  }

  @Test
  void shouldApplyRequestedScale_whenReturnsArePercentageValues() {
    PortfolioHolding holding = holding("ONLY", "100");
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(
        holding, returns("10", "-20"));

    NavigableMap<LocalDate, BigDecimal> result = component.calculateWeightedAverage(
        returns, ReturnFactorScale.SCALE_OF_TWO);

    assertThat(result).containsOnlyKeys(JANUARY, FEBRUARY);
    assertThat(result.get(JANUARY)).isEqualByComparingTo("1.1");
    assertThat(result.get(FEBRUARY)).isEqualByComparingTo("0.8");
  }

  @Test
  void shouldCalculateInitialValueWeights_whenEndingWeightsAreRequested() {
    PortfolioHolding firstHolding = holding("FIRST", "100");
    PortfolioHolding secondHolding = holding("SECOND", "300");
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(
        firstHolding, returns("10", "20"),
        secondHolding, returns("30", "40"));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> result = component.calculateEndingPortfolioWeight(returns);

    assertThat(result).containsOnlyKeys(firstHolding, secondHolding);
    assertWeightSeries(result.get(firstHolding), "0.25");
    assertWeightSeries(result.get(secondHolding), "0.75");
  }

  @ParameterizedTest
  @MethodSource("unavailableReturns")
  void shouldReturnEmptySeries_whenNoHoldingReturnsAreProvided(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns) {
    NavigableMap<LocalDate, BigDecimal> result = component.calculateWeightedAverage(
        returns, ReturnFactorScale.AS_IS);

    assertThat(result).isEmpty();
  }

  private static PortfolioHolding holding(String id, String value) {
    return new PortfolioHolding(new BigDecimal(value), FinancialInstrumentType.ETF, Country.CANADA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }

  private static TreeMap<LocalDate, BigDecimal> returns(String january, String february) {
    return new TreeMap<>(Map.of(
        JANUARY, new BigDecimal(january),
        FEBRUARY, new BigDecimal(february)));
  }

  private static Stream<Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>> unavailableReturns() {
    return Stream.of(null, Map.of());
  }

  private static void assertWeightSeries(TreeMap<LocalDate, BigDecimal> weights, String expectedWeight) {
    assertThat(weights).containsOnlyKeys(DECEMBER, JANUARY, FEBRUARY);
    assertThat(weights.values()).allSatisfy(weight -> assertThat(weight).isEqualByComparingTo(expectedWeight));
  }
}
