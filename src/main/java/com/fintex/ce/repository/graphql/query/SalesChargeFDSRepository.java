package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.SalesChargeEndpoint;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalesChargeSMRepository extends MultipleSMAbstractRepository<RSalesCharge, RSalesCharge, RSalesCharge, RSalesCharge> {

    @Autowired
    public SalesChargeSMRepository(final GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RSalesCharge> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                       final List<DataProvider> providers) {
        return doQuery(holdings, new SalesChargeEndpoint(), providers);
    }
}
