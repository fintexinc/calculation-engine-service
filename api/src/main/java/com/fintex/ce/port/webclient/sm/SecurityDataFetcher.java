package com.fintex.ce.port.webclient.sm;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.DataProvider;

import java.util.List;
import java.util.Map;

/**
 * Port interface for Security Master data queries.
 *
 * @param <T>
 *          domain model type returned by the query
 */
@FunctionalInterface
public interface SecurityDataFetcher<T> {

  Map<Holding, T> fetch(List<? extends Holding> holdings, List<DataProvider> providers);

}
