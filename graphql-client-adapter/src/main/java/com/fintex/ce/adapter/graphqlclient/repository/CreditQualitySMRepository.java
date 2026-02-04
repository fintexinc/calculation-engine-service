package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CreditQualitySMRepository
    extends
      MultipleSMAbstractRepository<CreditQuality, CreditQuality, CreditQuality, CreditQuality> {

  @Autowired
  public CreditQualitySMRepository(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, CreditQuality> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, CreditQuality> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, CreditQuality> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityEtfUsEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, CreditQuality> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, CreditQuality> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, CreditQuality> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, CreditQuality> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, CreditQuality> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new CreditQualityFixedIncomeEndpoint(), providers);
  }

}
