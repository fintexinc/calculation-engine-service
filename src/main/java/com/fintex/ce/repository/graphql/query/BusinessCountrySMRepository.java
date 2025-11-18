package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.BusinessCountryEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BusinessCountrySMRepository
        extends MultipleSMAbstractRepository<RBusinessCountry, RBusinessCountry, RBusinessCountry, RBusinessCountry> {

    @Autowired
    public BusinessCountrySMRepository(final GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<StockHolding, RBusinessCountry> queryBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new BusinessCountryEndpoint(), providers);
    }

}
