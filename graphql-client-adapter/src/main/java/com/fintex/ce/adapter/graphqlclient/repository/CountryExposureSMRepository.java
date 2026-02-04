package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CountryExposureSMRepository
    extends
      MultipleSMAbstractRepository<CountryExposure, CountryExposure, CountryExposure, CountryExposure> {

  @Autowired
  public CountryExposureSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, CountryExposure> queryBenchOfFundCanada(List<FundSeriesHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, CountryExposure> queryBenchOfEtfCanada(List<EtfHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, CountryExposure> queryBenchOfOfEtfUs(List<EtfHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureEtfUsEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, CountryExposure> queryBenchOfBenchmarks(List<BenchmarkIndexHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, CountryExposure> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, CountryExposure> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, CountryExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, CountryExposure> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new CountryExposureFixedIncomeEndpoint(), providers);
  }

}
