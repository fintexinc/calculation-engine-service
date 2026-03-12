package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.SmaHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class AssetAllocationGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<AssetAllocation> {

  @Autowired
  public AssetAllocationGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, AssetAllocation> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new AssetAllocationBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new AssetAllocationCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new AssetAllocationCanadaPooledFundEndpoint();
      case CANADA_ETF -> new AssetAllocationEtfCanadaEndpoint();
      case US_ETF -> new AssetAllocationEtfUsEndpoint();
      case FIXED_INCOME -> new AssetAllocationFixedIncomeEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new AssetAllocationFundCanadaEndpoint();
      case SEPARATELY_MANAGED_ACCOUNT -> new AssetAllocationSeparatelyManagedAccountEndpoint();
      case US_MUTUAL_FUNDS -> new AssetAllocationUsFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, AssetAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, AssetAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationEtfUsEndpoint(), providers);
  }

    public Map<EtfHolding, AssetAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationEtfCanadaEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, AssetAllocation> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, AssetAllocation> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, AssetAllocation> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationCanadaHedgeFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, AssetAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationUsFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, AssetAllocation> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationFixedIncomeEndpoint(), providers);
  }

    public Map<SmaHolding, AssetAllocation> queryBenchOfSeparatelyManagedAccounts(List<SmaHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new AssetAllocationSeparatelyManagedAccountEndpoint(), providers);
  }
}
