package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationCanadaHedgedFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class EquityCountryAllocationGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<EquityCountryAllocation> {

  @Autowired
  public EquityCountryAllocationGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, EquityCountryAllocation> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new EquityCountryAllocationBenchmarkEndpoint();
      case CANADA_POOLED_FUNDS -> new EquityCountryAllocationCanadaPooledFundEndpoint();
      case CANADA_ETF -> new EquityCountryAllocationEtfCanadaEndpoint();
      case US_ETF -> new EquityCountryAllocationEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new EquityCountryAllocationFundCanadaEndpoint();
      case US_MUTUAL_FUNDS -> new EquityCountryAllocationUsMutualFundEndpoint();
      case CANADA_HEDGE_FUNDS -> new EquityCountryAllocationCanadaHedgedFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, EquityCountryAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityCountryAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityCountryAllocation> queryBenchOfOfEtfUs(List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationEtfUsEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, EquityCountryAllocation> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, EquityCountryAllocation> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationCanadaPooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, EquityCountryAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, EquityCountryAllocation> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityCountryAllocationCanadaHedgedFundEndpoint(), providers);
  }
}
