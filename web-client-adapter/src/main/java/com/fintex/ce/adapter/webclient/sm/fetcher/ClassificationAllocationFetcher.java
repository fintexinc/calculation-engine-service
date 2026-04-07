package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.DataProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub implementation for ClassificationAllocation SecurityDataFetcher.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class ClassificationAllocationFetcher implements SecurityDataFetcher<ClassificationAllocation> {

  @Override
  public Map<Holding, ClassificationAllocation> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
