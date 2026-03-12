package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.SalesChargeEndpoint;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class SalesChargeGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<SalesCharge> {

  @Autowired
  public SalesChargeGraphqlDataFetcher(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, SalesCharge> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new SalesChargeEndpoint();
      default -> null;
    };
  }

    public Map<FundSeriesHolding, SalesCharge> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new SalesChargeEndpoint(), providers);
  }
}
