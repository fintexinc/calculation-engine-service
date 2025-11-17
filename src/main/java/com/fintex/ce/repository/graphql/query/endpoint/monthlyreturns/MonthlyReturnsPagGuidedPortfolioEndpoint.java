package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.smclient.graphql.PagGuidedPortfolioQuery;
import com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.repository.graphql.query.endpoint.core.PagGuidedPortfolioAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;

import static com.fintex.ce.config.enumeration.cache.CacheCategory.PAG_GUIDED_PORTFOLIO;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MONTHLY_RETURNS;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class MonthlyReturnsPagGuidedPortfolioEndpoint extends PagGuidedPortfolioAbstractEndpoint<RMonthlyReturns> {

    public MonthlyReturnsPagGuidedPortfolioEndpoint() {
        super(
                GraphQlEndpointConstants.GET_PAG_GUIDED_PORTFOLIOS,
                List.of(),
                buildCacheName(MONTHLY_RETURNS, PAG_GUIDED_PORTFOLIO)
        );
    }

    @Override
    public PagGuidedPortfolioQuery requestMapper(final PagGuidedPortfolioQuery query) {
        return query
                .monthlyReturns(
                        qMonthly -> qMonthly.returns(
                                qReturns -> qReturns.date().value()
                        ).dataProvider()
                )
                .identifier();
    }

    @Override
    public RMonthlyReturns responseMapper(final PagGuidedPortfolio pagGuidedPortfolio,
                                          final PagHolding holding) {
        return GraphQlMapperUtils.monthlyReturns(
                pagGuidedPortfolio.getMonthlyReturns(),
                holding.getCurrency().name(),
                holding
        );
    }

}
