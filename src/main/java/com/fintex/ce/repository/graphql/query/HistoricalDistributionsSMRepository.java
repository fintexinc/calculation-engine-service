package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HistoricalDistributionsSMRepository
        extends MultipleSMAbstractRepository<RHistoricalDistributions, RHistoricalDistributions, RHistoricalDistributions, RHistoricalDistributions> {

    @Autowired
    public HistoricalDistributionsSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RHistoricalDistributions> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                                    final List<DataProvider> providers) {
        // TODO: Implement proper GraphQL query endpoint when available
        return Map.of();
    }
}
