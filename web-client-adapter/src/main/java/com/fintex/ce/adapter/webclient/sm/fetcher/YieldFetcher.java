package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for Yield SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class YieldFetcher implements SecurityDataFetcher<Yield> {

  @Override
  public Map<Holding, Yield> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
