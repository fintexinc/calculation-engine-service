package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class YieldGraphqlDataFetcher extends AbstractGraphqlDataFetcher<Yield> {

  @Autowired
  public YieldGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, Yield> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new YieldFundCanadaEndpoint();
      case CANADA_ETF -> new YieldEtfCanadaEndpoint();
      case US_ETF -> new YieldEtfUsEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new YieldStockEndpoint();
      case US_MUTUAL_FUNDS -> new YieldUsMutualFundEndpoint();
      case CANADA_POOLED_FUNDS -> new YieldPooledFundEndpoint();
      case CANADA_HEDGE_FUNDS -> new YieldCanadaHedgeFundEndpoint();
      case FIXED_INCOME -> new YieldFixedIncomeEndpoint();
      case SEPARATELY_MANAGED_ACCOUNT -> new YieldSeparatelyManagedAccountEndpoint();
      default -> null;
    };
  }

    public Map<FundSeriesHolding, Yield> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, Yield> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, Yield> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldEtfUsEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, Yield> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldPooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, Yield> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, Yield> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldCanadaHedgeFundEndpoint(), providers);
  }

    public Map<StockHolding, Yield> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldStockEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, Yield> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldFixedIncomeEndpoint(), providers);
  }

    public Map<SmaHolding, Yield> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new YieldSeparatelyManagedAccountEndpoint(), providers);
  }

}
