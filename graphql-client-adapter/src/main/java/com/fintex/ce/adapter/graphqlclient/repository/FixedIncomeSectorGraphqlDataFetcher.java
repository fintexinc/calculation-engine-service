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
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class FixedIncomeSectorGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<FixedIncomeBondSecurities> {

  @Autowired
  public FixedIncomeSectorGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, FixedIncomeBondSecurities> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new FixedIncomeBondSectorBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new FixedIncomeBondSectorCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new FixedIncomeBondSectorCanadaPooledFundEndpoint();
      case CANADA_ETF -> new FixedIncomeBondSectorEtfCanadaEndpoint();
      case US_ETF -> new FixedIncomeBondSectorEtfUsEndpoint();
      case FIXED_INCOME -> new FixedIncomeBondSectorFixedIncomeEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new FixedIncomeBondSectorFundCanadaEndpoint();
      case US_MUTUAL_FUNDS -> new FixedIncomeBondSectorUsMutualFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, FixedIncomeBondSecurities> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, FixedIncomeBondSecurities> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorEtfUsEndpoint(), providers);
  }

    public Map<EtfHolding, FixedIncomeBondSecurities> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorEtfCanadaEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, FixedIncomeBondSecurities> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, FixedIncomeBondSecurities> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, FixedIncomeBondSecurities> queryCanadaHedgeFunds(
      final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorCanadaHedgeFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, FixedIncomeBondSecurities> queryUsMutualFunds(
      final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorUsMutualFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, FixedIncomeBondSecurities> queryBenchOfFixedIncomes(
      final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorFixedIncomeEndpoint(), providers);
  }

}
