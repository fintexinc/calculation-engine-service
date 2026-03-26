package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for ManagementFee SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class ManagementFeeFetcher implements SecurityDataFetcher<ManagementFee> {

  @Override
  public Map<Holding, ManagementFee> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
