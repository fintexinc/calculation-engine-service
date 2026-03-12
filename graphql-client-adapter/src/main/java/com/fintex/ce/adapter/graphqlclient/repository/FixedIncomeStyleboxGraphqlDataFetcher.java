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
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class FixedIncomeStyleboxGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<FixedIncomeStyleboxExposure> {

  @Autowired
  public FixedIncomeStyleboxGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, FixedIncomeStyleboxExposure> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new FixedIncomeStyleboxExposureBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint();
      case US_MUTUAL_FUNDS -> new FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint();
      case CANADA_ETF -> new FixedIncomeStyleboxExposureEtfCanadaEndpoint();
      case US_ETF -> new FixedIncomeStyleboxExposureEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new FixedIncomeStyleboxExposureFundCanadaEndpoint();
      case CANADA_POOLED_FUNDS -> new FixedIncomeStyleboxExposurePooledFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, FixedIncomeStyleboxExposure> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, FixedIncomeStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, FixedIncomeStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureEtfUsEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, FixedIncomeStyleboxExposure> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, FixedIncomeStyleboxExposure> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposurePooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, FixedIncomeStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, FixedIncomeStyleboxExposure> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint(), providers);
  }

}
