package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationPooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MaturityAllocationSMRepository
    extends
      MultipleSMAbstractRepository<MaturityAllocation, MaturityAllocation, MaturityAllocation, MaturityAllocation> {

  @Autowired
  public MaturityAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, MaturityAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, MaturityAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, MaturityAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationEtfUsEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, MaturityAllocation> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationPooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, MaturityAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationCanadaUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, MaturityAllocation> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, MaturityAllocation> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationFixedIncomeEndpoint(), providers);
  }

}
