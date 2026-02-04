package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityMarketCapitalizationSMRepository
    extends
      MultipleSMAbstractRepository<EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalization, EquityMarketCapitalizationStock> {

  @Autowired
  public EquityMarketCapitalizationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, EquityMarketCapitalization> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityMarketCapitalization> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityMarketCapitalization> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationEtfUsEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, EquityMarketCapitalizationStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationStockEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, EquityMarketCapitalization> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, EquityMarketCapitalization> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, EquityMarketCapitalization> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, EquityMarketCapitalization> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquityMarketCapitalizationUsMutualFundEndpoint(), providers);
  }
}
