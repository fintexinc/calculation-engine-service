package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.*;
import com.fintex.ce.model.redis.RClassificationAllocation;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.classificationallocation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ClassificationAllocationSMRepository extends MultipleSMAbstractRepository<RClassificationAllocation, RClassificationAllocation, RClassificationAllocation, RClassificationAllocation> {

    @Autowired
    public ClassificationAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RClassificationAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                                    final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RClassificationAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                                            final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RClassificationAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                                          final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationEtfUsEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RClassificationAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                                  final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, RClassificationAllocation> queryBenchOfStock(final List<StockHolding> holdings,
                                                                          final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationStockEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RClassificationAllocation> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new ClassificationAllocationFixedIncomeEndpoint(), providers);
    }

}
