package com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.smclient.graphql.PagGuidedPortfolioQuery;
import com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlEndpointConstants;
import com.fintex.ce.domain.model.holding.PagHolding;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.PagGuidedPortfolioAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.constant.CacheCategory.PAG_GUIDED_PORTFOLIO;
import static com.fintex.ce.constant.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsPagGuidedPortfolioEndpoint extends PagGuidedPortfolioAbstractEndpoint<MonthlyReturns> {

  public MonthlyReturnsPagGuidedPortfolioEndpoint() {
    super(
        GraphQlEndpointConstants.GET_PAG_GUIDED_PORTFOLIOS,
        List.of(),
        buildCacheName(MONTHLY_RETURNS, PAG_GUIDED_PORTFOLIO));
  }

  @Override
  public PagGuidedPortfolioQuery requestMapper(final PagGuidedPortfolioQuery query) {
    return query
        .monthlyReturns(
            qMonthly -> qMonthly.returns(
                qReturns -> qReturns.date().value()).dataProvider())
        .identifier();
  }

  @Override
  public MonthlyReturns responseMapper(final PagGuidedPortfolio pagGuidedPortfolio,
      final PagHolding holding) {
    return GraphQlMapperUtils.monthlyReturns(
        pagGuidedPortfolio.getMonthlyReturns(),
        holding.getCurrency().name(),
        holding);
  }

}
