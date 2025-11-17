package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldingsStock;
import com.fintex.ce.repository.graphql.query.endpoint.core.StockAbstractEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.HoldingIdentifierType.TICKER;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.STOCKS;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class CommonHoldingsStockEndpoint extends StockAbstractEndpoint<RCommonHoldingsStock> {

    public CommonHoldingsStockEndpoint() {
        super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(TOP_COMMON_HOLDINGS, STOCKS));
    }

    @Override
    public StockQuery requestMapper(final StockQuery query) {
        return query
                .companyName(STRING_DATAPOINT_QUERY_DEFINITION)
                .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Override
    public RCommonHoldingsStock responseMapper(final Stock stock, final StockHolding holding) {
        final ArrayList<DataErrorException> errors = new ArrayList<>();

        final String ticker = Optional.of(stock.getExternalIdentifiers().getCodes().stream()
                .filter(e -> TICKER.name().equalsIgnoreCase(e.getType().name())).map(ExternalIdentifierTypeValue::getValue).findFirst()).get().orElse(null);

        final String exchangeCode = Optional.of(stock.getExternalIdentifiers().getCodes().stream()
                .filter(e -> ExternalIdentifierType.EXCHANGE_ID.equals(e.getType())).map(ExternalIdentifierTypeValue::getValue).findFirst()).get().orElse(null);

        final String companyName = Optional.ofNullable(stock.getCompanyName()).map(StringDatapoint::getValue)
                .orElseGet(() -> {
                    errors.add(new DataErrorException("Company name does not exist for this stock.", ticker, ExceptionCode.WRN_CHS_001));
                    return null;
                });

        RCommonHoldingsStock rCommonHoldingsStock = new RCommonHoldingsStock(companyName, ticker, exchangeCode);
        rCommonHoldingsStock.setErrors(errors);

        return rCommonHoldingsStock;
    }

}
