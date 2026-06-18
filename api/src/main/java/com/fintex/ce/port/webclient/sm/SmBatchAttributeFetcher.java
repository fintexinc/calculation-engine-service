package com.fintex.ce.port.webclient.sm;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;

import java.util.List;

/**
 * Port for prefetching a union of SM attribute bundles in a single request, warming up
 * {@link com.fintex.ce.util.BatchContext} so subsequent per-fetcher calls are served from cache.
 */
public interface SmBatchAttributeFetcher {

  /**
   * Fetches all requested {@code bundles} for the given {@code holdings} in one SM call and stores the results in the
   * active {@link com.fintex.ce.util.BatchContext}. Must be called while a BatchContext is active.
   */
  void prefetchIntoContext(List<PortfolioHolding> holdings,
      List<SecurityAttributeBundle> bundles,
      List<DataProvider> providers);
}
