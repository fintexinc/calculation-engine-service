package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.UsMutualFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_US_FUND_BY_TICKERS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.US_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorUsMutualFundEndpoint extends UsMutualFundAbstractEndpoint<FixedIncomeBondSecurities> {

  public FixedIncomeBondSectorUsMutualFundEndpoint() {
    super(GET_US_FUND_BY_TICKERS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES, US_MUTUAL_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
      UnaryOperator<UsFundQuery> preDefinedFDSQuery) {
    return q -> q.getUsFundsByTickers(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public UsFundQuery requestMapper(UsFundQuery query) {
    return query
        .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeBondSecurities responseMapper(UsFund fund, UsMutualFundHolding holding) {
    FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = fund.getFixedIncomeSecuritiesAllocation();
    return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
  }

}
