package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsPagGuidedPortfolioEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsSeparatelyManagedAccountEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsStockEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns.MonthlyReturnsUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MonthlyReturnsSMRepository
        extends MultipleSMAbstractRepository<RMonthlyReturns, RMonthlyReturns, RMonthlyReturns, RMonthlyReturns> {

    @Autowired
    public MonthlyReturnsSMRepository(final GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RMonthlyReturns> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RMonthlyReturns> queryBenchOfOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsEtfUsEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RMonthlyReturns> queryBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, RMonthlyReturns> queryBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsStockEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RMonthlyReturns> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
                                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RMonthlyReturns> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                        final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RMonthlyReturns> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
                                                                                final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RMonthlyReturns> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RMonthlyReturns> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                             final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsFixedIncomeEndpoint(), providers);
    }

    @Override
    public Map<SmaHolding, RMonthlyReturns> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
                                                                                  final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsSeparatelyManagedAccountEndpoint(), providers);
    }

    @Override
    public Map<PagHolding, RMonthlyReturns> queryBenchOfPagGuidedPortfolios(final List<PagHolding> holdings,
                                                                            final List<DataProvider> providers) {
        return doQuery(holdings, new MonthlyReturnsPagGuidedPortfolioEndpoint(), providers);
    }
}
