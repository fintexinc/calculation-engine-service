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
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FixedIncomeBondSectorSMRepository extends MultipleSMAbstractRepository<RFixedIncomeBondSecurities, RFixedIncomeBondSecurities, RFixedIncomeBondSecurities, RFixedIncomeBondSecurities> {

    @Autowired
    public FixedIncomeBondSectorSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RFixedIncomeBondSecurities> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RFixedIncomeBondSecurities> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorEtfUsEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RFixedIncomeBondSecurities> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RFixedIncomeBondSecurities> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
                                                                                         final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RFixedIncomeBondSecurities> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
                                                                                           final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RFixedIncomeBondSecurities> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                                         final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RFixedIncomeBondSecurities> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                                   final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RFixedIncomeBondSecurities> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                                        final List<DataProvider> providers) {
        return doQuery(holdings, new FixedIncomeBondSectorFixedIncomeEndpoint(), providers);
    }

}
