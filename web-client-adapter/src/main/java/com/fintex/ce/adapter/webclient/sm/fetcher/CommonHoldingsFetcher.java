package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.model.domain.calculation.holding.CommonHoldings;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub implementation for CommonHoldings SecurityDataFetcher. TODO: Replace with actual REST implementation.
 */
@Component
public class CommonHoldingsFetcher implements SecurityDataFetcher<CommonHoldings> {

  @Override
  public Map<Holding, CommonHoldings> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
