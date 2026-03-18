package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for ClassificationAllocation SecurityDataPort.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class ClassificationAllocationSecurityDataPortStub implements SecurityDataPort<ClassificationAllocation> {

  @Override
  public Map<Holding, ClassificationAllocation> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
