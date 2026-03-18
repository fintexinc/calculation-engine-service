package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.port.output.FxRatesPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for FxRatesPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class FxRatesPortStub implements FxRatesPort {

  @Override
  public Map<LocalDate, FxRates.FxRate> loadFxRates() {
    return new HashMap<>();
  }
}
