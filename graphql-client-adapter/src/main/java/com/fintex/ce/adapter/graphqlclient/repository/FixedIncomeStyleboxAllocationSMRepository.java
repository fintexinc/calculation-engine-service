package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FixedIncomeStyleboxAllocationSMRepository
    extends
      MultipleSMAbstractRepository<FixedIncomeStyleboxExposure, FixedIncomeStyleboxExposure, FixedIncomeStyleboxExposure, FixedIncomeStyleboxExposure> {

  @Autowired
  public FixedIncomeStyleboxAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, FixedIncomeStyleboxExposure> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, FixedIncomeStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, FixedIncomeStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureEtfUsEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, FixedIncomeStyleboxExposure> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, FixedIncomeStyleboxExposure> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposurePooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, FixedIncomeStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, FixedIncomeStyleboxExposure> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint(), providers);
  }

}
