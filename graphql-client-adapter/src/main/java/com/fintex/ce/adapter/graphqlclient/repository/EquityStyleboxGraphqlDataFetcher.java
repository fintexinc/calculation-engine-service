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
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class EquityStyleboxGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<EquityStyleboxExposure> {

  @Autowired
  public EquityStyleboxGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, EquityStyleboxExposure> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case BENCHMARK_INDEX -> new EquityStyleboxExposureBenchmarkEndpoint();
      case CANADA_HEDGE_FUNDS -> new EquityStyleboxExposureCanadaHedgeFundEndpoint();
      case US_MUTUAL_FUNDS -> new EquityStyleboxExposureCanadaUsMutualFundEndpoint();
      case CANADA_ETF -> new EquityStyleboxExposureEtfCanadaEndpoint();
      case US_ETF -> new EquityStyleboxExposureEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new EquityStyleboxExposureFundCanadaEndpoint();
      case CANADA_POOLED_FUNDS -> new EquityStyleboxExposurePooledFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, EquityStyleboxExposure> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, EquityStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureEtfUsEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, EquityStyleboxExposure> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, EquityStyleboxExposure> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposurePooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, EquityStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, EquityStyleboxExposure> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureCanadaHedgeFundEndpoint(), providers);
  }
}
