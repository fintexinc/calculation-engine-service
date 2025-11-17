package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeEtfCanadaEndpoint extends EtfAbstractEndpoint<RManagementFee> {

    public ManagementFeeEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(MANAGEMENT_FEE, CANADA_ETF));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .managementFee(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RManagementFee responseMapper(final Etf etf, final EtfHolding etfHolding) {
        final var managementFeeEtfCanada = new RManagementFee();
        Optional.ofNullable(etf.getManagementFee()).ifPresent(result -> managementFeeEtfCanada.setProvider(DataProvider.of(result.getDataProvider()).name()));
        return managementFeeEtfCanada.setManagementFee(getBigDecimalOrNull(etf.getManagementFee()));
    }

}
