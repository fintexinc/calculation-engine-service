package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HistoricalNavPricesSMRepository
        extends MultipleSMAbstractRepository<RHistoricalNavPrices, RHistoricalNavPrices, RHistoricalNavPrices, RHistoricalNavPrices> {

    @Autowired
    public HistoricalNavPricesSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RHistoricalNavPrices> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                                final List<DataProvider> providers) {
        // TODO: TODO: clarify to either remove the logic entirely or finish it
        return Map.of();
    }
}
