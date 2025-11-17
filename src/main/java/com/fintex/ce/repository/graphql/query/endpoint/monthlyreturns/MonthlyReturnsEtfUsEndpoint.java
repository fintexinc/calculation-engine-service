package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsEtfUsEndpoint extends EtfAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsEtfUsEndpoint() {
        super(GET_US_ETFS_BY_TICKERS, List.of(DataProvider.MORNINGSTAR), buildCacheName(MONTHLY_RETURNS, US_ETF));
    }

    public MonthlyReturnsEtfUsEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                       final List<DataProvider> supportedProviders,
                                       final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .currency(p -> p.permittedDataProviders(loadProviders()), c -> c.type().dataProvider())
                .monthlyReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.value().date()
                        ).dataProvider()
                )
                .ticker(t -> t.value().dataProvider());
    }

    @Override
    public RMonthlyReturns responseMapper(final Etf etf, final EtfHolding holding) {
        if (etf.getCurrency() == null || etf.getCurrency().getType() == null) {
            return GraphQlMapperUtils.monthlyReturns(etf.getMonthlyReturns(), null, holding);
        }
        return GraphQlMapperUtils.monthlyReturns(etf.getMonthlyReturns(), etf.getCurrency().getType().name(), holding);
    }

}
