package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationCanadaHedgedFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitycountryallocation.EquityCountryAllocationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityCountryAllocationSMRepository
        extends MultipleSMAbstractRepository<REquityCountryAllocation, REquityCountryAllocation, REquityCountryAllocation, RedisId> {

    @Autowired
	public EquityCountryAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, REquityCountryAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityCountryAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityCountryAllocation> queryBenchOfOfEtfUs(List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationEtfUsEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, REquityCountryAllocation> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, REquityCountryAllocation> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, REquityCountryAllocation> queryUsMutualFunds(List<UsMutualFundHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, REquityCountryAllocation> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityCountryAllocationCanadaHedgedFundEndpoint(), providers);
    }
}
