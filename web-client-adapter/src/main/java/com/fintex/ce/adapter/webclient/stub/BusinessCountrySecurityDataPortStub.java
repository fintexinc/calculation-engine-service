package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for BusinessCountry SecurityDataPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class BusinessCountrySecurityDataPortStub implements SecurityDataPort<BusinessCountry> {

  @Override
  public Map<Holding, BusinessCountry> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
