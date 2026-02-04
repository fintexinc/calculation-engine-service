package com.fintex.ce.adapter.graphqlclient.endpoint.managementfee;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.EtfAbstractEndpoint;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_ETFS_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_ETF;
import static com.fintex.ce.constant.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.CacheUtils.buildCacheName;
import static com.fintex.ce.adapter.graphqlclient.util.GraphqlUtil.getBigDecimalOrNull;

public class ManagementFeeEtfUsEndpoint extends EtfAbstractEndpoint<ManagementFee> {

  public ManagementFeeEtfUsEndpoint() {
    super(GET_US_ETFS_BY_TICKERS, List.of(), buildCacheName(MANAGEMENT_FEE, US_ETF));
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
    return q -> q.getUsEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
  }

  @Override
  public EtfQuery requestMapper(final EtfQuery query) {
    return query
        .managementFee(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION)
        .ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Override
  public ManagementFee responseMapper(final Etf etf, final EtfHolding etfHolding) {
    final var managementFeeEtfUs = new ManagementFee();
    Optional.ofNullable(etf.getManagementFee()).ifPresent(result -> managementFeeEtfUs.setProvider(DataProvider.of(
        result.getDataProvider().name()).name()));
    return managementFeeEtfUs.setManagementFee(getBigDecimalOrNull(etf.getManagementFee()));
  }

}
