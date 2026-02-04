package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class YieldSMRepository extends MultipleSMAbstractRepository<Yield, Yield, Yield, Yield> {

  @Autowired
  public YieldSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, Yield> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, Yield> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, Yield> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldEtfUsEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, Yield> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldPooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, Yield> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, Yield> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, Yield> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldStockEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, Yield> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldFixedIncomeEndpoint(), providers);
  }

  @Override
  public Map<SmaHolding, Yield> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldSeparatelyManagedAccountEndpoint(), providers);
  }

}
