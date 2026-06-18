package com.fintex.ce.application.calculation.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SmBatchAttributeFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SmBatchPrefetcherTest {

  @Mock
  private SmBatchAttributeFetcher batchFetcher;

  private SmBatchPrefetcher prefetcher;

  @BeforeEach
  void setUp() {
    prefetcher = new SmBatchPrefetcher(batchFetcher);
  }

  @Test
  void shouldPrefetchPortfolioHoldings_whenMetricsRequireSmData() {
    List<CalculationMetric> metrics = List.of(CalculationMetric.ASSET_ALLOCATIONS);
    List<PortfolioHolding> holdings = List.of(holding("XBAL"));
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    prefetcher.prefetch(metrics, holdings, List.of(), providers);

    verify(batchFetcher).prefetchIntoContext(eq(holdings), any(), eq(providers));
  }

  @Test
  void shouldPrefetchBenchmarkHoldings_whenBenchmarkHoldingsArePresent() {
    List<CalculationMetric> metrics = List.of(CalculationMetric.TRAILING_TOTAL_RETURNS);
    List<PortfolioHolding> holdings = List.of(holding("XBAL"));
    List<PortfolioHolding> benchmarkHoldings = List.of(holding("XIU"));
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    prefetcher.prefetch(metrics, holdings, benchmarkHoldings, providers);

    verify(batchFetcher).prefetchIntoContext(eq(holdings), any(), eq(providers));
    verify(batchFetcher).prefetchIntoContext(eq(benchmarkHoldings), any(), eq(providers));
  }

  @Test
  void shouldSkipBenchmarkPrefetch_whenBenchmarkHoldingsAreEmpty() {
    List<CalculationMetric> metrics = List.of(CalculationMetric.ASSET_ALLOCATIONS);
    List<PortfolioHolding> holdings = List.of(holding("XBAL"));
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    prefetcher.prefetch(metrics, holdings, List.of(), providers);

    verify(batchFetcher).prefetchIntoContext(eq(holdings), any(), eq(providers));
    verify(batchFetcher, never()).prefetchIntoContext(eq(List.of()), any(), any());
  }

  @Test
  void shouldSkipAllPrefetching_whenMetricsListIsEmpty() {
    prefetcher.prefetch(List.of(), List.of(holding("XBAL")), List.of(), List.of(DataProvider.MORNINGSTAR));

    verifyNoInteractions(batchFetcher);
  }

  @Test
  void shouldSkipAllPrefetching_whenMetricsHaveNoRegisteredBundles() {
    prefetcher.prefetch(
        List.of(CalculationMetric.COMMON_PERFORMANCE_DATES),
        List.of(holding("XBAL")),
        List.of(),
        List.of(DataProvider.MORNINGSTAR));

    verifyNoInteractions(batchFetcher);
  }

  @Test
  void shouldSkipPortfolioPrefetch_whenHoldingsAreEmpty() {
    prefetcher.prefetch(
        List.of(CalculationMetric.ASSET_ALLOCATIONS),
        List.of(),
        List.of(),
        List.of(DataProvider.MORNINGSTAR));

    verifyNoInteractions(batchFetcher);
  }

  @Test
  void shouldSwallowException_andNotPropagateToCallers() {
    List<CalculationMetric> metrics = List.of(CalculationMetric.ASSET_ALLOCATIONS);
    List<PortfolioHolding> holdings = List.of(holding("XBAL"));
    List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

    doThrow(new RuntimeException("SM batch endpoint unavailable"))
        .when(batchFetcher).prefetchIntoContext(any(), any(), any());

    prefetcher.prefetch(metrics, holdings, List.of(), providers);

    verify(batchFetcher).prefetchIntoContext(any(), any(), any());
  }

  private static PortfolioHolding holding(String ticker) {
    var identifier = new SecurityIdentifier();
    identifier.setId(ticker);
    identifier.setIdType(FiIdentifierType.TICKER);
    return new PortfolioHolding(BigDecimal.valueOf(10_000), FinancialInstrumentType.ETF_CANADA, identifier);
  }
}
