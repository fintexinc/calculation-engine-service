package com.fintex.ce.port.output.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;

import java.util.List;

public interface HoldingDataLoader<T> {

  T load(List<? extends Holding> holdings, List<DataProvider> providers,
      List<Warning> warnings, ParamHolderDTO paramHolderDTO);

}
