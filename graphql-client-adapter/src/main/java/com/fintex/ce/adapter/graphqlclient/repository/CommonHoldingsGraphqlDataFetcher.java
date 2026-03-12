package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class CommonHoldingsGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<CommonHoldings> {

  @Autowired
  public CommonHoldingsGraphqlDataFetcher(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, CommonHoldings> resolveEndpoint(HoldingType type) {
    AbstractSMEndpoint<?, ?, ?, ?, ?> endpoint = switch (type) {
      case BENCHMARK_INDEX -> new CommonHoldingsBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new CommonHoldingsCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new CommonHoldingsCanadaPooledFundEndpoint();
      case CANADA_ETF -> new CommonHoldingsEtfCanadaEndpoint();
      case US_ETF -> new CommonHoldingsEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new CommonHoldingsFundCanadaEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new CommonHoldingsStockEndpoint();
      case US_MUTUAL_FUNDS -> new CommonHoldingsUsMutualFundEndpoint();
      default -> null;
    };
    return (AbstractSMEndpoint<?, ?, ?, ?, CommonHoldings>) endpoint;
  }


    public Map<EtfHolding, CommonHoldings> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, CommonHoldings> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsEtfUsEndpoint(), providers);
  }

    public Map<FundSeriesHolding, CommonHoldings> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsFundCanadaEndpoint(), providers);
  }

    public Map<StockHolding, CommonHoldingsStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsStockEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, CommonHoldings> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, CommonHoldings> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, CommonHoldings> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsCanadaHedgeFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, CommonHoldings> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsUsMutualFundEndpoint(), providers);
  }
}
