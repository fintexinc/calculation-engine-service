package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for EquityStyleboxExposure SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class EquityStyleboxExposureFetcher implements SecurityDataFetcher<EquityStyleboxExposure> {

  @Override
  public Map<Holding, EquityStyleboxExposure> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
