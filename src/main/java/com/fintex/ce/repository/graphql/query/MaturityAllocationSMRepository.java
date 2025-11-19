package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationPooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MaturityAllocationSMRepository extends MultipleSMAbstractRepository<RMaturityAllocation, RMaturityAllocation, RMaturityAllocation, RedisId> {

    @Autowired
    public MaturityAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RMaturityAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RMaturityAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RMaturityAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationEtfUsEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RMaturityAllocation> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationPooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RMaturityAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationCanadaUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RMaturityAllocation> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RMaturityAllocation> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new MaturityAllocationFixedIncomeEndpoint(), providers);
    }

}
