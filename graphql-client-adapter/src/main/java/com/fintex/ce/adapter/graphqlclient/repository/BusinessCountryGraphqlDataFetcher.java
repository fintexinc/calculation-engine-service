package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.BusinessCountryEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class BusinessCountryGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<BusinessCountry> {

  @Autowired
  public BusinessCountryGraphqlDataFetcher(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, BusinessCountry> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_STOCKS, US_STOCKS -> new BusinessCountryEndpoint();
      default -> null;
    };
  }

    public Map<StockHolding, BusinessCountry> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new BusinessCountryEndpoint(), providers);
  }

}
