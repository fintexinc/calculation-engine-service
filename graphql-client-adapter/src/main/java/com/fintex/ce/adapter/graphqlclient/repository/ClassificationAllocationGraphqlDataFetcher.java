package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.*;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class ClassificationAllocationGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<ClassificationAllocation> {

  @Autowired
  public ClassificationAllocationGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, ClassificationAllocation> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new ClassificationAllocationFundCanadaEndpoint();
      case CANADA_ETF -> new ClassificationAllocationEtfCanadaEndpoint();
      case US_ETF -> new ClassificationAllocationEtfUsEndpoint();
      case US_MUTUAL_FUNDS -> new ClassificationAllocationUsMutualFundEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new ClassificationAllocationStockEndpoint();
      case FIXED_INCOME -> new ClassificationAllocationFixedIncomeEndpoint();
      default -> null;
    };
  }

    public Map<FundSeriesHolding, ClassificationAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, ClassificationAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, ClassificationAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationEtfUsEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, ClassificationAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationUsMutualFundEndpoint(), providers);
  }

    public Map<StockHolding, ClassificationAllocation> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationStockEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, ClassificationAllocation> queryBenchOfFixedIncomes(
      final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ClassificationAllocationFixedIncomeEndpoint(), providers);
  }

}
