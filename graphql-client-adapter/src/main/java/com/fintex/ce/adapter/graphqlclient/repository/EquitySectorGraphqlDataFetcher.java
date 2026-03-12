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
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class EquitySectorGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<EquitySector> {

  @Autowired
  public EquitySectorGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, EquitySector> resolveEndpoint(HoldingType type) {
    AbstractSMEndpoint<?, ?, ?, ?, ?> endpoint = switch (type) {
      case BENCHMARK_INDEX -> new EquitySectorBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new EquitySectorCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new EquitySectorCanadaPooledFundEndpoint();
      case US_MUTUAL_FUNDS -> new EquitySectorCanadaUsMutualFundEndpoint();
      case CANADA_ETF -> new EquitySectorEtfCanadaEndpoint();
      case US_ETF -> new EquitySectorEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new EquitySectorFundCanadaEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new EquitySectorStockEndpoint();
      default -> null;
    };
    return (AbstractSMEndpoint<?, ?, ?, ?, EquitySector>) endpoint;
  }


    public Map<FundSeriesHolding, EquitySector> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquitySector> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquitySector> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorEtfUsEndpoint(), providers);
  }

    public Map<StockHolding, EquitySectorStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorStockEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, EquitySector> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, EquitySector> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaPooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, EquitySector> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, EquitySector> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaHedgeFundEndpoint(), providers);
  }
}
