package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassificationAllocationSMRepository
    extends
      MultipleSMAbstractRepository<ClassificationAllocation, ClassificationAllocation, ClassificationAllocation, ClassificationAllocation> {

  @Autowired
  public ClassificationAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, ClassificationAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, ClassificationAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, ClassificationAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationEtfUsEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, ClassificationAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, ClassificationAllocation> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationStockEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, ClassificationAllocation> queryBenchOfFixedIncomes(
      final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationFixedIncomeEndpoint(), providers);
  }

}
