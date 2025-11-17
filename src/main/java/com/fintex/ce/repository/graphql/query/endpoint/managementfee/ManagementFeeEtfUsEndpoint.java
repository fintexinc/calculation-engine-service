package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.US_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.util.graphql.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeEtfUsEndpoint extends EtfAbstractEndpoint<RManagementFee> {

    public ManagementFeeEtfUsEndpoint() {
        super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(MANAGEMENT_FEE, US_ETF));
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(final EtfQuery query) {
        return query
                .managementFee(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
                .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Override
    public RManagementFee responseMapper(final Etf etf, final EtfHolding etfHolding) {
        final var managementFeeEtfUs = new RManagementFee();
        Optional.ofNullable(etf.getManagementFee()).ifPresent(result -> managementFeeEtfUs.setProvider(DataProvider.of(result.getDataProvider()).name()));
        return managementFeeEtfUs.setManagementFee(getBigDecimalOrNull(etf.getManagementFee()));
    }

}
