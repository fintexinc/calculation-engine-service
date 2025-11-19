package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.*;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CountryExposureSMRepository
        extends MultipleSMAbstractRepository<RCountryExposure, RCountryExposure, RCountryExposure, RedisId> {

    @Autowired
    public CountryExposureSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RCountryExposure> queryBenchOfFundCanada(List<FundSeriesHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RCountryExposure> queryBenchOfEtfCanada(List<EtfHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RCountryExposure> queryBenchOfOfEtfUs(List<EtfHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureEtfUsEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RCountryExposure> queryBenchOfBenchmarks(List<BenchmarkIndexHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RCountryExposure> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RCountryExposure> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RCountryExposure> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RCountryExposure> queryBenchOfFixedIncomes(List<FixedIncomeHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CountryExposureFixedIncomeEndpoint(), providers);
    }

}
