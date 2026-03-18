package com.fintex.ce.adapter.webclient.stub;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.HoldingDataLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for IncomeForecast HoldingDataLoader.
 * TODO: Replace with actual REST implementation.
 */
@Component
public class IncomeForecastDataLoaderStub implements HoldingDataLoader<Map<Holding, IncomeForecast>> {

  @Override
  public Map<Holding, IncomeForecast> load(List<? extends Holding> holdings, List<DataProvider> providers,
      List<Warning> warnings, ParamHolderDTO paramHolderDTO) {
    return new HashMap<>();
  }
}
