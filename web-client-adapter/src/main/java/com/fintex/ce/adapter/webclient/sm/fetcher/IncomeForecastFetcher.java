package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.model.domain.calculation.yield.IncomeForecast;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IncomeForecastFetcher implements SecurityDataFetcher<IncomeForecast> {

  @Override
  public Map<Holding, IncomeForecast> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    return new HashMap<>();
  }
}
