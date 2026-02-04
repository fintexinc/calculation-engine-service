package com.fintex.ce.application.calculation.core;// package com.fintex.ce.domain.calculation.core;
//
// import com.fintex.ce.dto.calculation.WeightedAverageInputDTO;
// import com.fintex.ce.dto.holding.Holding;
// import com.fintex.ce.util.DateTimeUtils;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.Test;
//
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.Map;
// import java.util.TreeMap;
//
// import static com.fintex.ce.config.enumeration.HoldingType.CASH;
// import static com.fintex.ce.config.enumeration.HoldingType.US_ETF;
// import static com.fintex.ce.config.enumeration.Rebalanced.MONTHLY;
// import static com.fintex.ce.util.ComparisonUtils.compareMaps;
// import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
// import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
// import static java.math.BigDecimal.*;
// import static org.mockito.Mockito.*;
//
// class WeightedAveragePortfolioMReturnsCalculationTest {
//
// @Test
// void calculatePortfolioBaseTotalReturn_verifyHoldingPortfolioBaseTotalReturn() {
// //SETUP
// final Map<LocalDate, BigDecimal> map = Map.of(LOCAL_DATE_NOW, ONE);
//
// final WeightedAverageInputDTO input = mock(WeightedAverageInputDTO.class);
// when(input.getFxRates()).thenReturn(Map.of());
// when(input.getPortfolioReturns()).thenReturn(Map.of(mock(Holding.class), map));
//
// final WeightedAveragePortfolioMReturnsCalculation cWeighted
// = mock(WeightedAveragePortfolioMReturnsCalculation.class, withSettings().useConstructor(input));
// doCallRealMethod().when(cWeighted).calculatePortfolioBaseTotalReturn();
//
// //ACT
// cWeighted.calculatePortfolioBaseTotalReturn();
//
// //VERIFY
// verify(cWeighted).holdingPortfolioBaseTotalReturn(eq(null), eq(map));
// }
//
// @Test
// void holdingPortfolioBaseTotalReturn_verifyHoldingPortfolioBaseTotalReturnFormula() {
// //SETUP
// final Map<LocalDate, BigDecimal> pReturns = Map.of(LOCAL_DATE_NOW, ONE);
// final Map<LocalDate, BigDecimal> fxRates = Map.of();
//
// final WeightedAveragePortfolioMReturnsCalculation cWeighted =
// mock(WeightedAveragePortfolioMReturnsCalculation.class);
// when(cWeighted.holdingPortfolioBaseTotalReturnFormula(any(), any(), anyMap())).thenReturn(ZERO);
// doCallRealMethod().when(cWeighted).holdingPortfolioBaseTotalReturn(anyMap(), anyMap());
//
// //ACT
// cWeighted.holdingPortfolioBaseTotalReturn(fxRates, pReturns);
//
// //VERIFY
// verify(cWeighted).holdingPortfolioBaseTotalReturn(eq(fxRates), eq(pReturns));
// }
//
// @Test
// void holdingPortfolioBaseTotalReturnFormula_calculatedProperly() {
// //SETUP
// final Map<LocalDate, BigDecimal> fxRates = Map.of(
// DateTimeUtils.toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), ONE,
// DateTimeUtils.toLastDayOfMonth(LOCAL_DATE_NOW), ONE);
//
// final WeightedAveragePortfolioMReturnsCalculation cWeighted =
// mock(WeightedAveragePortfolioMReturnsCalculation.class);
// doCallRealMethod().when(cWeighted).holdingPortfolioBaseTotalReturnFormula(any(), any(), anyMap());
//
// //ACT
// final BigDecimal expected =
// cWeighted.holdingPortfolioBaseTotalReturnFormula(DateTimeUtils.toLastDayOfMonth(LOCAL_DATE_NOW), ZERO, fxRates);
//
// //VERIFY
// Assertions.assertEquals(ZERO.compareTo(expected), 0);
// }
//
// @Test
// void calculateWeightedAveragePortfolioMReturns_verifyCalculatePortfolioBaseTotalReturn() {
// //SETUP
// final WeightedAveragePortfolioMReturnsCalculation cWeighted = mock(WeightedAveragePortfolioMReturnsCalculation.class,
// withSettings().useConstructor(mock(WeightedAverageInputDTO.class)));
// doCallRealMethod().when(cWeighted).calculateWeightedAveragePortfolioMReturns();
//
// //ACT
// cWeighted.calculateWeightedAveragePortfolioMReturns();
//
// //VERIFY
// verify(cWeighted).calculatePortfolioBaseTotalReturn();
// }
//
// @Test
// void calculateWeightedAveragePortfolioMReturns_verifyCalculateEndingPortfolioWeight() {
// //SETUP
// final WeightedAveragePortfolioMReturnsCalculation cWeighted = mock(WeightedAveragePortfolioMReturnsCalculation.class,
// withSettings().useConstructor(mock(WeightedAverageInputDTO.class)));
//
// final Map<Holding, Map<LocalDate, BigDecimal>> map = Map.of();
// when(cWeighted.calculatePortfolioBaseTotalReturn()).thenReturn(map);
//
// doCallRealMethod().when(cWeighted).calculateWeightedAveragePortfolioMReturns();
//
// //ACT
// cWeighted.calculateWeightedAveragePortfolioMReturns();
//
// //VERIFY
// verify(cWeighted).calculateEndingPortfolioWeight(eq(map));
// }
//
// @Test
// void calculateWeightedAveragePortfolioMReturns_verifyCalculateTotalPortfolioReturnFactor() {
// //SETUP
// final WeightedAveragePortfolioMReturnsCalculation cWeighted = mock(WeightedAveragePortfolioMReturnsCalculation.class,
// withSettings().useConstructor(mock(WeightedAverageInputDTO.class)));
//
// final Map<Holding, Map<LocalDate, BigDecimal>> map = Map.of();
// final Map<Holding, Map<LocalDate, BigDecimal>> endingPortfolioWeight = Map.of();
// when(cWeighted.calculatePortfolioBaseTotalReturn()).thenReturn(map);
// when(cWeighted.calculateEndingPortfolioWeight(eq(map))).thenReturn(endingPortfolioWeight);
//
// doCallRealMethod().when(cWeighted).calculateWeightedAveragePortfolioMReturns();
//
// //ACT
// cWeighted.calculateWeightedAveragePortfolioMReturns();
//
// //VERIFY
// verify(cWeighted).calculateTotalPortfolioReturnFactor(eq(map), eq(endingPortfolioWeight));
// }
//
// @Test
// void calculateEndingPortfolioWeight_verifyGetInitialHoldingWeights() {
// //SETUP
// final WeightedAverageInputDTO in = mock(WeightedAverageInputDTO.class);
// when(in.getRebalanced()).thenReturn(MONTHLY);
//
// final WeightedAveragePortfolioMReturnsCalculation cWeighted = mock(WeightedAveragePortfolioMReturnsCalculation.class,
// withSettings().useConstructor(in));
//
// doCallRealMethod().when(cWeighted).calculateEndingPortfolioWeight(anyMap());
//
// //ACT
// cWeighted.calculateEndingPortfolioWeight(Map.of());
//
// //VERIFY
// verify(cWeighted).getInitialHoldingWeights();
// }
//
// @Test
// void calculateEndingPortfolioWeight_verifyFormattedCorrectlyForMonthlyRebalancing() {
// //SETUP
// final WeightedAverageInputDTO in = mock(WeightedAverageInputDTO.class);
// when(in.getRebalanced()).thenReturn(MONTHLY);
//
// final WeightedAveragePortfolioMReturnsCalculation cWeighted = mock(WeightedAveragePortfolioMReturnsCalculation.class,
// withSettings().useConstructor(in));
//
// final Holding h1 = mock(Holding.class);
// when(h1.getType()).thenReturn(CASH);
// final Holding h2 = mock(Holding.class);
// when(h1.getType()).thenReturn(US_ETF);
//
// when(cWeighted.getInitialHoldingWeights()).thenReturn(Map.of(h1, TEN, h2, ZERO));
// doCallRealMethod().when(cWeighted).calculateEndingPortfolioWeight(anyMap());
//
// //ACT
// final Map<Holding, Map<LocalDate, BigDecimal>> actual = cWeighted.calculateEndingPortfolioWeight(
// Map.of(h1, new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE)), h2, new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE))));
//
// //VERIFY
// final LocalDate dayBeforeStart = toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1));
// compareMaps(Map.of(h1, Map.of(dayBeforeStart, TEN, LOCAL_DATE_NOW, TEN), h2, Map.of(dayBeforeStart, ZERO,
// LOCAL_DATE_NOW, ZERO)), actual);
// }
//
// }