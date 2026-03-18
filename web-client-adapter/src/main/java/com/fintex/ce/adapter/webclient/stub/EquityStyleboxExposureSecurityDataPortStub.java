package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for EquityStyleboxExposure SecurityDataPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class EquityStyleboxExposureSecurityDataPortStub implements SecurityDataPort<EquityStyleboxExposure> {

  @Override
  public Map<Holding, EquityStyleboxExposure> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
