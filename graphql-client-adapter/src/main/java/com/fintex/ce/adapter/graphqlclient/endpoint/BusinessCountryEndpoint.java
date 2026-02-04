package com.fintex.ce.adapter.graphqlclient.endpoint;

import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.StockAbstractEndpoint;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.STOCKS;
import static com.fintex.ce.constant.CacheNameEntity.BUSINESS_COUNTRY;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

/**
 * Only for stocks
 */
public class BusinessCountryEndpoint extends StockAbstractEndpoint<BusinessCountry> {

  public BusinessCountryEndpoint() {
    super(GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS, List.of(), buildCacheName(BUSINESS_COUNTRY, STOCKS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<EquityIdentifiers> equityIdentifiers,
      UnaryOperator<StockQuery> preDefinedFDSQuery) {
    return q -> q.getStocksByTickersAndExchangeIds(equityIdentifiers, preDefinedFDSQuery::apply);
  }

  @Override
  public StockQuery requestMapper(final StockQuery query) {
    return query
        .businessCountry(STRING_WITH_DATA_PROVIDER_DEFINITION)
        .externalIdentifiers(
            id -> id.codes(
                qCodes -> qCodes.value().type()));
  }

  @Override
  public BusinessCountry responseMapper(final Stock stock, final StockHolding holding) {
    final var businessCountry = new BusinessCountry();
    if (Objects.nonNull(stock) && Objects.nonNull(stock.getBusinessCountry())) {
      final var smDataProvider = stock.getBusinessCountry().getDataProvider();
      final var provider = smDataProvider != null ? DataProvider.of(smDataProvider.name()) : null;
      if (provider == null) {
        throw new SystemException("Invalid or missing data provider", ErrorCode.INTERNAL_SERVER_ERROR);
      }
      businessCountry.setProvider(provider.name());
      businessCountry.setValue(stock.getBusinessCountry().getValue());
    }
    return businessCountry;
  }
}
