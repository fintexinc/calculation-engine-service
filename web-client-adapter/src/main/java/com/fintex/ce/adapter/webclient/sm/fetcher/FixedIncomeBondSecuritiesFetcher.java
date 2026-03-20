package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for FixedIncomeBondSecurities SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class FixedIncomeBondSecuritiesFetcher implements SecurityDataFetcher<FixedIncomeBondSecurities> {

  @Override
  public Map<Holding, FixedIncomeBondSecurities> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
