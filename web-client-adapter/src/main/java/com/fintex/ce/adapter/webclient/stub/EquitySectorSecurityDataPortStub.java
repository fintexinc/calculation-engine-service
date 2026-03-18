package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for EquitySector SecurityDataPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class EquitySectorSecurityDataPortStub implements SecurityDataPort<EquitySector> {

  @Override
  public Map<Holding, EquitySector> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
