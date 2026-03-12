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
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class CreditQualityGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<CreditQuality> {

  @Autowired
  public CreditQualityGraphqlDataFetcher(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, CreditQuality> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new CreditQualityBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new CreditQualityCanadaHedgeFundEndpoint();
      case CANADA_POOLED_FUNDS -> new CreditQualityCanadaPooledFundEndpoint();
      case CANADA_ETF -> new CreditQualityEtfCanadaEndpoint();
      case US_ETF -> new CreditQualityEtfUsEndpoint();
      case FIXED_INCOME -> new CreditQualityFixedIncomeEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new CreditQualityFundCanadaEndpoint();
      case US_MUTUAL_FUNDS -> new CreditQualityUsMutualFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, CreditQuality> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, CreditQuality> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, CreditQuality> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityEtfUsEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, CreditQuality> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityBenchmarkEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, CreditQuality> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, CreditQuality> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, CreditQuality> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityCanadaHedgeFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, CreditQuality> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityFixedIncomeEndpoint(), providers);
  }

}
