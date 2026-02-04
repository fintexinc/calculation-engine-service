package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.domain.enumeration.HoldingIdentifierType.TICKER;
import static com.fintex.ce.constant.CacheCategory.STOCKS;
import static com.fintex.ce.constant.CacheNameEntity.TOP_COMMON_HOLDINGS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils.toValidationError;

public class CommonHoldingsStockEndpoint extends StockAbstractEndpoint<CommonHoldingsStock> {

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
  public CommonHoldingsStock responseMapper(final Stock stock, final StockHolding holding) {
    final ArrayList<ValidationError> errors = new ArrayList<>();

    final String ticker = Optional.of(stock.getExternalIdentifiers().getCodes().stream()
        .filter(e -> TICKER.name().equalsIgnoreCase(e.getType().name())).map(ExternalIdentifierTypeValue::getValue)
        .findFirst()).get().orElse(null);

    final String exchangeCode = Optional.of(stock.getExternalIdentifiers().getCodes().stream()
        .filter(e -> ExternalIdentifierType.EXCHANGE_ID.equals(e.getType())).map(ExternalIdentifierTypeValue::getValue)
        .findFirst()).get().orElse(null);

    final String companyName = Optional.ofNullable(stock.getCompanyName()).map(StringDatapoint::getValue)
        .orElseGet(() -> {
          errors.add(toValidationError(new DataErrorException("Company name does not exist for this stock.", ticker,
              ExceptionCode.WRN_CHS_001)));
          return null;
        });

    CommonHoldingsStock rCommonHoldingsStock = new CommonHoldingsStock(companyName, ticker, exchangeCode);
    rCommonHoldingsStock.setErrors(errors);

    return rCommonHoldingsStock;
  }

}
