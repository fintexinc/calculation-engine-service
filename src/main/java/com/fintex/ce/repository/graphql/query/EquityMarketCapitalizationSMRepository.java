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
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalizationStock;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationStockEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitymarketcapitalization.EquityMarketCapitalizationUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EquityMarketCapitalizationSMRepository
        extends MultipleSMAbstractRepository<REquityMarketCapitalization, REquityMarketCapitalization, REquityMarketCapitalization, REquityMarketCapitalizationStock> {

    @Autowired
	public EquityMarketCapitalizationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, REquityMarketCapitalization> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityMarketCapitalization> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, REquityMarketCapitalization> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationEtfUsEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, REquityMarketCapitalizationStock> queryBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationStockEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, REquityMarketCapitalization> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, REquityMarketCapitalization> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, REquityMarketCapitalization> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, REquityMarketCapitalization> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new EquityMarketCapitalizationUsMutualFundEndpoint(), providers);
    }
}
