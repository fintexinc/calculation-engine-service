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
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquitySectorSMRepository
    extends
      MultipleSMAbstractRepository<EquitySector, EquitySector, EquitySector, EquitySectorStock> {

  @Autowired
  public EquitySectorSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, EquitySector> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquitySector> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquitySector> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorEtfUsEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, EquitySectorStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorStockEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, EquitySector> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, EquitySector> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, EquitySector> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, EquitySector> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquitySectorCanadaHedgeFundEndpoint(), providers);
  }
}
