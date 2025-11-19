package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IncomeForecastSMRepository extends MultipleSMAbstractRepository<RIncomeForecast, RIncomeForecast, RIncomeForecast, RIncomeForecast> {

    @Autowired
    public IncomeForecastSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RIncomeForecast> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                          final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RIncomeForecast> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                                  final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RIncomeForecast> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                                final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastEtfUsEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RIncomeForecast> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
                                                                                final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastPooledFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RIncomeForecast> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                        final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastCanadaUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RIncomeForecast> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RIncomeForecast> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                             final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastFixedIncomeEndpoint(), providers);
    }

    @Override
    public Map<StockHolding, RIncomeForecast> queryBenchOfStock(final List<StockHolding> holdings,
                                                                final List<DataProvider> providers) {
        return doQuery(holdings, new IncomeForecastStockEndpoint(), providers);
    }

}
