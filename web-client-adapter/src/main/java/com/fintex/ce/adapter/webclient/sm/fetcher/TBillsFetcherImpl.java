package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.port.webclient.TBillsFetcher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for TBillsPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class TBillsFetcherImpl implements TBillsFetcher {

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(Currency currency) {
    return new TreeMap<>();
  }
}
