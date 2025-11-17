package com.fintex.ce.repository.graphql.query.endpoint;

import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.BUSINESS_COUNTRY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

/**
 * Only for stocks
 */
public class BusinessCountryEndpoint extends StockAbstractEndpoint<RBusinessCountry> {

    public BusinessCountryEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(BUSINESS_COUNTRY, STOCKS));
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<EquityIdentifiers> equityIdentifiers, UnaryOperator<StockQuery> preDefinedFDSQuery) {
        return q -> q.getStocksByTickersAndExchangeIds(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .businessCountry(STRING_WITH_DATA_PROVIDER_DEFINITION)
                .externalIdentifiers(
                        id -> id.codes(
                                qCodes -> qCodes.value().type()
                        )
                );
    }

    @Override
    public RBusinessCountry responseMapper(final Stock stock, final StockHolding holding) {
        final var rBusinessCountry = new RBusinessCountry();
        if (Objects.nonNull(stock) && Objects.nonNull(stock.getBusinessCountry())) {
            rBusinessCountry.setProvider(DataProvider.of((stock.getBusinessCountry().getDataProvider())).name());
            rBusinessCountry.setValue(stock.getBusinessCountry().getValue());
        }
        return rBusinessCountry;
    }
}
