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
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsStockEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommonHoldingsSMRepository
    extends
      MultipleSMAbstractRepository<CommonHoldings, CommonHoldings, CommonHoldings, CommonHoldingsStock> {

  @Autowired
  public CommonHoldingsSMRepository(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<EtfHolding, CommonHoldings> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, CommonHoldings> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsEtfUsEndpoint(), providers);
  }

  @Override
  public Map<FundSeriesHolding, CommonHoldings> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, CommonHoldingsStock> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsStockEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, CommonHoldings> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, CommonHoldings> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, CommonHoldings> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, CommonHoldings> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CommonHoldingsUsMutualFundEndpoint(), providers);
  }
}
