package com.fintex.ce.repository.graphql.query.endpoint.monthlyreturns;

import com.fintex.smclient.graphql.CurrencyType;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.smclient.graphql.PagGuidedPortfolioQuery;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsPagGuidedPortfolioEndpointTest {

    @Test
    void queryDefinition_verify() {
        //SETUP
        final MonthlyReturnsPagGuidedPortfolioEndpoint sut = mock(MonthlyReturnsPagGuidedPortfolioEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String identifier = "PAG001";
        final List<String> identifiers = List.of(identifier);

        doCallRealMethod().when(sut).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = sut.queryDefinition(identifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getPagGuidedPortfolios(eq(identifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final MonthlyReturnsPagGuidedPortfolioEndpoint sut = mock(MonthlyReturnsPagGuidedPortfolioEndpoint.class);

        final PagGuidedPortfolioQuery pagGuidedPortfolioQuery = mock(PagGuidedPortfolioQuery.class);
        when(pagGuidedPortfolioQuery.identifier()).thenReturn(pagGuidedPortfolioQuery);
        when(pagGuidedPortfolioQuery.monthlyReturns(any())).thenReturn(pagGuidedPortfolioQuery);

        doCallRealMethod().when(sut).requestMapper(any());
        //ACT
        final PagGuidedPortfolioQuery actual = sut.requestMapper(pagGuidedPortfolioQuery);

        //VERIFY
        verify(actual).identifier();
        verify(actual).monthlyReturns(any());
    }

    @Test
    void responseMapper_verifyMonthlyReturns() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final MonthlyReturnsPagGuidedPortfolioEndpoint sut = mock(MonthlyReturnsPagGuidedPortfolioEndpoint.class);

            final PagGuidedPortfolio pagGuidedPortfolio = mock(PagGuidedPortfolio.class);
            final MonthlyReturns monthlyReturns = mock(MonthlyReturns.class);
            when(pagGuidedPortfolio.getMonthlyReturns()).thenReturn(monthlyReturns);

            final PagHolding pagHolding = mock(PagHolding.class);
            final HoldingType holdingType = HoldingType.PAG_GUIDED_PORTFOLIO;
            final CurrencyType cad = CurrencyType.CAD;
            when(pagHolding.getType()).thenReturn(holdingType);
            when(pagHolding.getCurrency()).thenReturn(Currency.CAD);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(pagGuidedPortfolio, pagHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.monthlyReturns(monthlyReturns, cad.name(), pagHolding));
        }
    }

}
