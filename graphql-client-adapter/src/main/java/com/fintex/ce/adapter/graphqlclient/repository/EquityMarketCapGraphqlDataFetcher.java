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
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class EquityMarketCapGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<EquityMarketCapitalization> {

  @Autowired
  public EquityMarketCapGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, EquityMarketCapitalization> resolveEndpoint(HoldingType type) {
    AbstractSMEndpoint<?, ?, ?, ?, ?> endpoint = switch (type) {
      case BENCHMARK_INDEX -> new EquityMarketCapitalizationBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new EquityMarketCapitalizationCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new EquityMarketCapitalizationCanadaPooledFundEndpoint();
      case CANADA_ETF -> new EquityMarketCapitalizationEtfCanadaEndpoint();
      case US_ETF -> new EquityMarketCapitalizationEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new EquityMarketCapitalizationFundCanadaEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new EquityMarketCapitalizationStockEndpoint();
      case US_MUTUAL_FUNDS -> new EquityMarketCapitalizationUsMutualFundEndpoint();
      default -> null;
    };
    return (AbstractSMEndpoint<?, ?, ?, ?, EquityMarketCapitalization>) endpoint;
  }


    public Map<FundSeriesHolding, EquityMarketCapitalization> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityMarketCapitalization> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityMarketCapitalization> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationEtfUsEndpoint(), providers);
  }

    public Map<StockHolding, EquityMarketCapitalizationStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationStockEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, EquityMarketCapitalization> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, EquityMarketCapitalization> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, EquityMarketCapitalization> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationCanadaHedgeFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, EquityMarketCapitalization> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationUsMutualFundEndpoint(), providers);
  }
}
