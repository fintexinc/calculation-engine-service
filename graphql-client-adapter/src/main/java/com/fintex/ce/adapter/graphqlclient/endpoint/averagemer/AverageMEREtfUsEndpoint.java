package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_ETF;
import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getDataProviderOrNull;
import static java.util.Optional.ofNullable;

public class AverageMEREtfUsEndpoint extends EtfAbstractEndpoint<AverageMer> {

  public AverageMEREtfUsEndpoint() {
    super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(MER, US_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
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
  public AverageMer responseMapper(final Etf etf, final EtfHolding etfHolding) {
    final var result = new AverageMer();

    result.setNetExpenseRatio(getBigDecimalOrNull(etf.getNetExpenseRatio()));
    result.setGrossExpenseRatio(getBigDecimalOrNull(etf.getGrossExpenseRatio()));

    ofNullable(getDataProviderOrNull(etf.getNetExpenseRatio()))
        .ifPresent(dp -> result.setNetExpenseRatioProvider(dp.name()));

    ofNullable(getDataProviderOrNull(etf.getGrossExpenseRatio()))
        .ifPresent(dp -> result.setGrossExpenseRatioProvider(dp.name()));

    return result;
  }

}
