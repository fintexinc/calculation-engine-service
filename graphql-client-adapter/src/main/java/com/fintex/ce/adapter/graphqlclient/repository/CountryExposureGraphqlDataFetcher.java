package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class CountryExposureGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<CountryExposure> {

  @Autowired
  public CountryExposureGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, CountryExposure> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new CountryExposureFundCanadaEndpoint();
      case CANADA_ETF -> new CountryExposureEtfCanadaEndpoint();
      case US_ETF -> new CountryExposureEtfUsEndpoint();
      case BENCHMARK_INDEX -> new CountryExposureBenchmarkEndpoint();
      case CANADA_POOLED_FUNDS -> new CountryExposureCanadaPooledFundEndpoint();
      case CANADA_HEDGE_FUNDS -> new CountryExposureCanadaHedgeFundEndpoint();
      case US_MUTUAL_FUNDS -> new CountryExposureUsMutualFundEndpoint();
      case FIXED_INCOME -> new CountryExposureFixedIncomeEndpoint();
      default -> null;
    };
  }

    public Map<FundSeriesHolding, CountryExposure> queryBenchOfFundCanada(List<FundSeriesHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, CountryExposure> queryBenchOfEtfCanada(List<EtfHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, CountryExposure> queryBenchOfOfEtfUs(List<EtfHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureEtfUsEndpoint(), providers);
  }

    public Map<BenchmarkIndexHolding, CountryExposure> queryBenchOfBenchmarks(List<BenchmarkIndexHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureBenchmarkEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, CountryExposure> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureCanadaPooledFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, CountryExposure> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureCanadaHedgeFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, CountryExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureUsMutualFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, CountryExposure> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureFixedIncomeEndpoint(), providers);
  }

}
