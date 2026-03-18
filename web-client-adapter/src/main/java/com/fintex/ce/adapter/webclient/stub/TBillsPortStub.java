package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.output.TBillsPort;
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
public class TBillsPortStub implements TBillsPort {

  @Override
  public NavigableMap<LocalDate, BigDecimal> loadTBillsFor(Currency currency) {
    return new TreeMap<>();
  }
}
