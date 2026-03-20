package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.port.FxRatesFetcher;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for FxRatesPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class FxRatesFetcherStub implements FxRatesFetcher {

  @Override
  public Map<LocalDate, FxRates.FxRate> fetch() {
    return new HashMap<>();
  }
}
