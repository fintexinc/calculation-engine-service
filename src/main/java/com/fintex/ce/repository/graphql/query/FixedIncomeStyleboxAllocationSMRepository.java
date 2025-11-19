package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeStyleboxExposure;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FixedIncomeStyleboxAllocationSMRepository extends MultipleSMAbstractRepository<RFixedIncomeStyleboxExposure, RFixedIncomeStyleboxExposure, RFixedIncomeStyleboxExposure, RedisId> {

    @Autowired
    public FixedIncomeStyleboxAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RFixedIncomeStyleboxExposure> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RFixedIncomeStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RFixedIncomeStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureEtfUsEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RFixedIncomeStyleboxExposure> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RFixedIncomeStyleboxExposure> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposurePooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RFixedIncomeStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RFixedIncomeStyleboxExposure> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeStyleboxExposureCanadaHedgeFundEndpoint(), providers);
    }

}
