package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.creditquality.CreditQualityUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CreditQualitySMRepository
        extends MultipleSMAbstractRepository<RCreditQuality, RCreditQuality, RCreditQuality, RedisId> {

    @Autowired
	public CreditQualitySMRepository(final GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RCreditQuality> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                         final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RCreditQuality> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                                 final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RCreditQuality> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                               final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityEtfUsEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RCreditQuality> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
                                                                             final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RCreditQuality> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                       final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RCreditQuality> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
                                                                               final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RCreditQuality> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                             final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RCreditQuality> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                            final List<DataProvider> providers) {
        return doQuery(holdings, new CreditQualityFixedIncomeEndpoint(), providers);
    }

}
