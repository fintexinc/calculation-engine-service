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
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FixedIncomeBondSectorSMRepository
    extends
      MultipleSMAbstractRepository<FixedIncomeBondSecurities, FixedIncomeBondSecurities, FixedIncomeBondSecurities, FixedIncomeBondSecurities> {

  @Autowired
  public FixedIncomeBondSectorSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, FixedIncomeBondSecurities> queryBenchOfFundCanada(
      final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, FixedIncomeBondSecurities> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorEtfUsEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, FixedIncomeBondSecurities> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<BenchmarkIndexHolding, FixedIncomeBondSecurities> queryBenchOfBenchmarks(
      final List<BenchmarkIndexHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorBenchmarkEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, FixedIncomeBondSecurities> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorCanadaPooledFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, FixedIncomeBondSecurities> queryCanadaHedgeFunds(
      final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, FixedIncomeBondSecurities> queryUsMutualFunds(
      final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, FixedIncomeBondSecurities> queryBenchOfFixedIncomes(
      final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new FixedIncomeBondSectorFixedIncomeEndpoint(), providers);
  }

}
