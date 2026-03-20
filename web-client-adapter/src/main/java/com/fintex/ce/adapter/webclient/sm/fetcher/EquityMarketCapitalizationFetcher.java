package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for EquityMarketCapitalization SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class EquityMarketCapitalizationFetcher implements SecurityDataFetcher<EquityMarketCapitalization> {

  @Override
  public Map<Holding, EquityMarketCapitalization> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
