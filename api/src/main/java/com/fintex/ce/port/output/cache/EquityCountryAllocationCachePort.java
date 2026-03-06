package com.fintex.ce.port.output.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EquityCountryAllocationCachePort
    extends HoldingDataLoader<Map<Holding, Map<CountryRegionType, BigDecimal>>> {

  Map<Holding, Map<CountryRegionType, BigDecimal>> loadWithDataProvidersCheck(
      List<? extends Holding> holdings, List<DataProvider> providers, List<Warning> warnings);

}
