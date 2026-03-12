package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationPooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class MaturityAllocationGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<MaturityAllocation> {

  @Autowired
  public MaturityAllocationGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, MaturityAllocation> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_HEDGE_FUNDS -> new MaturityAllocationCanadaHedgeFundEndpoint();
      case US_MUTUAL_FUNDS -> new MaturityAllocationCanadaUsMutualFundEndpoint();
      case CANADA_ETF -> new MaturityAllocationEtfCanadaEndpoint();
      case US_ETF -> new MaturityAllocationEtfUsEndpoint();
      case FIXED_INCOME -> new MaturityAllocationFixedIncomeEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new MaturityAllocationFundCanadaEndpoint();
      case CANADA_POOLED_FUNDS -> new MaturityAllocationPooledFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, MaturityAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, MaturityAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, MaturityAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationEtfUsEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, MaturityAllocation> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationPooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, MaturityAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationCanadaUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, MaturityAllocation> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationCanadaHedgeFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, MaturityAllocation> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings,
      List<DataProvider> providers) {
    return doQuery(holdings, new MaturityAllocationFixedIncomeEndpoint(), providers);
  }

}
