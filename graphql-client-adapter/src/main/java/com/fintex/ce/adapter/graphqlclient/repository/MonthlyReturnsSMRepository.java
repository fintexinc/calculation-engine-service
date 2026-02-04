package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
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
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsPagGuidedPortfolioEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MonthlyReturnsSMRepository
    extends
      MultipleSMAbstractRepository<MonthlyReturns, MonthlyReturns, MonthlyReturns, MonthlyReturns> {

  @Autowired
  public MonthlyReturnsSMRepository(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, MonthlyReturns> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, MonthlyReturns> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsEtfUsEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, MonthlyReturns> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, MonthlyReturns> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsStockEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, MonthlyReturns> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, MonthlyReturns> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, MonthlyReturns> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, MonthlyReturns> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, MonthlyReturns> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsFixedIncomeEndpoint(), providers);
  }

  @Override
  public Map<SmaHolding, MonthlyReturns> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsSeparatelyManagedAccountEndpoint(), providers);
  }

  @Override
  public Map<PagHolding, MonthlyReturns> queryBenchOfPagGuidedPortfolios(final List<PagHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MonthlyReturnsPagGuidedPortfolioEndpoint(), providers);
  }
}
