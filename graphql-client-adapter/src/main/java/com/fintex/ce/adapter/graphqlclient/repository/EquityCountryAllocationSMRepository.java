package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationCanadaHedgedFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityCountryAllocationSMRepository
    extends
      MultipleSMAbstractRepository<EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation, EquityCountryAllocation> {

  @Autowired
  public EquityCountryAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, EquityCountryAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityCountryAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityCountryAllocation> queryBenchOfOfEtfUs(List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationEtfUsEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, EquityCountryAllocation> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, EquityCountryAllocation> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, EquityCountryAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, EquityCountryAllocation> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationCanadaHedgedFundEndpoint(), providers);
  }
}
