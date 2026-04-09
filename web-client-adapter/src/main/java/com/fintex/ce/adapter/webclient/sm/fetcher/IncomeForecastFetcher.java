package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.DataProvider;

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
