package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposureFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitystyleboxexposure.EquityStyleboxExposurePooledFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityStyleboxAllocationSMRepository extends MultipleSMAbstractRepository<REquityStyleboxExposure, REquityStyleboxExposure, REquityStyleboxExposure, RedisId> {

    @Autowired
    public EquityStyleboxAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, REquityStyleboxExposure> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityStyleboxExposure> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityStyleboxExposure> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureEtfUsEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, REquityStyleboxExposure> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, REquityStyleboxExposure> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposurePooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, REquityStyleboxExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureCanadaUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, REquityStyleboxExposure> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityStyleboxExposureCanadaHedgeFundEndpoint(), providers);
    }
}
