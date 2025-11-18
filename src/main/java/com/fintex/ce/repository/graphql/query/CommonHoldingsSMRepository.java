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
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldings;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldingsStock;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsStockEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.commonholdings.CommonHoldingsUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommonHoldingsSMRepository
        extends MultipleSMAbstractRepository<RCommonHoldings, RCommonHoldings, RCommonHoldings, RCommonHoldingsStock> {

    @Autowired
    public CommonHoldingsSMRepository(final GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<EtfHolding, RCommonHoldings> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RCommonHoldings> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsEtfUsEndpoint(), providers);
    }

    @Override
    public Map<FundSeriesHolding, RCommonHoldings> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, RCommonHoldingsStock> queryBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsStockEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RCommonHoldings> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RCommonHoldings> queryCanadaPooledFunds(List<CanadaPooledFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RCommonHoldings> queryCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RCommonHoldings> queryUsMutualFunds(List<UsMutualFundHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new CommonHoldingsUsMutualFundEndpoint(), providers);
    }
}
