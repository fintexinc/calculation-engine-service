package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityStyleboxAllocationSMRepository
    extends
      MultipleSMAbstractRepository<EquityStyleboxExposure, EquityStyleboxExposure, EquityStyleboxExposure, EquityStyleboxExposure> {

  @Autowired
  public EquityStyleboxAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, EquityStyleboxExposure> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, EquityStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureEtfUsEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, EquityStyleboxExposure> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, EquityStyleboxExposure> queryCanadaPooledFunds(
      List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposurePooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, EquityStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, EquityStyleboxExposure> queryCanadaHedgeFunds(
      List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
    return doQuery(holdings, new EquityStyleboxExposureCanadaHedgeFundEndpoint(), providers);
  }
}
