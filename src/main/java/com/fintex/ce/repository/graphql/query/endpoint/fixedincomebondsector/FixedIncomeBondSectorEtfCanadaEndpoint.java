package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorEtfCanadaEndpoint extends EtfAbstractEndpoint<RFixedIncomeBondSecurities> {

    public FixedIncomeBondSectorEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, CANADA_ETF));
    }

    public FixedIncomeBondSectorEtfCanadaEndpoint(final Function<Query, List<Etf>> getFDSEntityFunction,
                                                  final List<DataProvider> supportedProviders,
                                                  final String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }
    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
                                                final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(EtfQuery query) {
        return query
                .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RFixedIncomeBondSecurities responseMapper(final Etf etf, final EtfHolding holding) {
        final FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = etf.getFixedIncomeSecuritiesAllocation();
        return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
    }

}
