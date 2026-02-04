package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationSeparatelyManagedAccountEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationUsFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AssetAllocationSMRepository
    extends
      MultipleSMAbstractRepository<AssetAllocation, AssetAllocation, AssetAllocation, AssetAllocation> {

  @Autowired
  public AssetAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, AssetAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, AssetAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationEtfUsEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, AssetAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, AssetAllocation> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, AssetAllocation> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, AssetAllocation> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, AssetAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationUsFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, AssetAllocation> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationFixedIncomeEndpoint(), providers);
  }

  @Override
  public Map<SmaHolding, AssetAllocation> queryBenchOfSeparatelyManagedAccounts(List<SmaHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationSeparatelyManagedAccountEndpoint(), providers);
  }
}
