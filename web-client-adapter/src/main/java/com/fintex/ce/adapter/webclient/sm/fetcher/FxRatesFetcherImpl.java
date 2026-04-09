package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.port.webclient.FxRatesFetcher;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation for FxRatesPort. TODO: Replace with actual REST implementation.
 */
@Component
public class FxRatesFetcherImpl implements FxRatesFetcher {

  @Override
  public Map<LocalDate, FxRates.FxRate> fetch() {
    return new HashMap<>();
  }
}
