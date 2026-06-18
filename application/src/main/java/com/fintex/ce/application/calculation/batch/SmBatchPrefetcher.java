package com.fintex.ce.application.calculation.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SmBatchAttributeFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmBatchPrefetcher {

  private final SmBatchAttributeFetcher batchFetcher;

  public void prefetch(List<CalculationMetric> metrics,
      List<PortfolioHolding> holdings,
      List<PortfolioHolding> benchmarkHoldings,
      List<DataProvider> providers) {
    if (CollectionUtils.isEmpty(metrics)) {
      return;
    }
    Set<SecurityAttributeBundle> needed = MetricBundleRegistry.bundlesFor(metrics);
    if (needed.isEmpty()) {
      return;
    }
    List<SecurityAttributeBundle> bundles = List.copyOf(needed);
    prefetchHoldings(holdings, bundles, providers);
    prefetchHoldings(benchmarkHoldings, bundles, providers);
  }

  private void prefetchHoldings(List<PortfolioHolding> holdings,
      List<SecurityAttributeBundle> bundles,
      List<DataProvider> providers) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    try {
      batchFetcher.prefetchIntoContext(holdings, bundles, providers);
    } catch (Exception e) {
      log.warn("SM batch pre-fetch failed — calculations will fall back to individual SM calls: {}",
          e.getMessage());
    }
  }
}
