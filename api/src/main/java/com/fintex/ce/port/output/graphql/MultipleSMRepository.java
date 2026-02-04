package com.fintex.ce.port.output.graphql;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.PagHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;

import java.util.List;
import java.util.Map;

import static com.fintex.ce.constant.GeneralConstants.METHOD_NOT_IMPLEMENTED;

/**
 * Port interface for multiple SM (Morningstar) entity queries. Returns domain models instead of Redis entities.
 *
 * @param <F>
 *          Fund Canada domain model type
 * @param <C>
 *          ETF Canada domain model type
 * @param <U>
 *          ETF US domain model type
 * @param <S>
 *          Stock domain model type
 */
public interface MultipleSMRepository<F, C, U, S> extends SingleSMRepository<F, C, U> {

  /**
   * For only for CANADA_MUTUAL_FUND
   *
   * @return mapped object
   */
  default Map<FundSeriesHolding, F> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  /**
   * Could work for both Canada ETF & US ETF
   *
   * @return mapped object
   */
  default Map<EtfHolding, C> queryBenchOfEtf(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  /**
   * Could work for both Canada Stock & US Stock
   *
   * @return mapped object
   */
  default Map<StockHolding, S> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<EtfHolding, C> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<EtfHolding, U> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<BenchmarkIndexHolding, F> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<UsMutualFundHolding, F> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<CanadaPooledFundHolding, F> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<CanadaHedgeFundHolding, F> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<FixedIncomeHolding, F> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<SmaHolding, F> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

  default Map<PagHolding, F> queryBenchOfPagGuidedPortfolios(final List<PagHolding> holdings,
      final List<DataProvider> providers) {
    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
  }

}
