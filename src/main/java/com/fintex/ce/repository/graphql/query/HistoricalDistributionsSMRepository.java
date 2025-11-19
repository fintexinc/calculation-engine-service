package com.fintex.ce.repository.graphql.query;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.smclient.service.GraphqlTransportComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // TODO: clarify to either remove the logic entirely or finish it
        return new HashMap<>();
    }
}
