package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.sm.model.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for BusinessCountry SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class BusinessCountryFetcher implements SecurityDataFetcher<BusinessCountry> {

  @Override
  public Map<Holding, BusinessCountry> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
