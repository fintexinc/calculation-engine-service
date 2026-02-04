package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.BusinessCountryEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BusinessCountrySMRepository
    extends
      MultipleSMAbstractRepository<BusinessCountry, BusinessCountry, BusinessCountry, BusinessCountry> {

  @Autowired
  public BusinessCountrySMRepository(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<StockHolding, BusinessCountry> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new BusinessCountryEndpoint(), providers);
  }

}
