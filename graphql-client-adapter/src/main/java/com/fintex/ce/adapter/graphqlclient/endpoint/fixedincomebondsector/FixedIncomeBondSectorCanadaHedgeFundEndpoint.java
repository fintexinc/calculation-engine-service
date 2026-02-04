package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.CanadaHedgeFundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants.GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.constant.CacheCategory.CANADA_HEDGE_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.FIXED_INCOME_BOND_SECURITIES;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class FixedIncomeBondSectorCanadaHedgeFundEndpoint
    extends
      CanadaHedgeFundAbstractEndpoint<FixedIncomeBondSecurities> {

  public FixedIncomeBondSectorCanadaHedgeFundEndpoint() {
    super(GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS, List.of(), buildCacheName(FIXED_INCOME_BOND_SECURITIES,
        CANADA_HEDGE_FUNDS));
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> morningstarIds,
      UnaryOperator<HedgeFundQuery> preDefinedFDSQuery) {
    return q -> q.getCanadaHedgeFundsByMorningstarIds(morningstarIds, preDefinedFDSQuery::apply);
  }

  @Override
  public HedgeFundQuery requestMapper(HedgeFundQuery query) {
    return query
        .fixedIncomeSecuritiesAllocation(sa -> sa.allocation(a -> a.name().value()).dataProvider())
        .externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Override
  public FixedIncomeBondSecurities responseMapper(HedgeFund fund, CanadaHedgeFundHolding holding) {
    FixedIncomeSecuritiesAllocation fixedIncomeSecuritiesAllocation = fund.getFixedIncomeSecuritiesAllocation();
    return GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSecuritiesAllocation, holding.getType());
  }

}
