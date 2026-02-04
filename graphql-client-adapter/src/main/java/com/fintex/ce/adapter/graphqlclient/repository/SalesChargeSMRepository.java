package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.SalesChargeEndpoint;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalesChargeSMRepository
    extends
      MultipleSMAbstractRepository<SalesCharge, SalesCharge, SalesCharge, SalesCharge> {

  @Autowired
  public SalesChargeSMRepository(final GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, SalesCharge> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new SalesChargeEndpoint(), providers);
  }
}
