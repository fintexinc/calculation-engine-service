package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.util.graphql.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMEREtfUsEndpoint extends EtfAbstractEndpoint<RAverageMer> {

    public AverageMEREtfUsEndpoint() {
        super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(MER, US_ETF));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .netExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .grossExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RAverageMer responseMapper(final Etf etf, final EtfHolding etfHolding) {
        final var result = new RAverageMer();

        result.setNetExpenseRatio(getBigDecimalOrNull(etf.getNetExpenseRatio()));
        result.setGrossExpenseRatio(getBigDecimalOrNull(etf.getGrossExpenseRatio()));

        ofNullable(etf.getNetExpenseRatio())
                .ifPresent(net -> result.setNetExpenseRatioProvider(getDataProviderOrNull(net)));

        ofNullable(etf.getGrossExpenseRatio())
                .ifPresent(gross -> result.setGrossExpenseRatioProvider(getDataProviderOrNull(gross)));

        return result;
    }

}
