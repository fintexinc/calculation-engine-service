package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.model.redis.equitysector.REquitySectorStock;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquitySectorSMRepository
        extends MultipleSMAbstractRepository<REquitySector, REquitySector, REquitySector, REquitySectorStock> {

    @Autowired
	public EquitySectorSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, REquitySector> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquitySector> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquitySector> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorEtfUsEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, REquitySectorStock> queryBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorStockEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, REquitySector> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, REquitySector> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, REquitySector> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorCanadaUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, REquitySector> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquitySectorCanadaHedgeFundEndpoint(), providers);
    }
}
