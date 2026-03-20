package com.fintex.ce.port.sm;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;

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
