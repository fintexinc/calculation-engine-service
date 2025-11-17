package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeBondSectorFixedIncomeEndpointTest {

    @Test
    void requestMapper_verify() {
        //SETUP
        final FixedIncomeBondSectorFixedIncomeEndpoint sut = mock(FixedIncomeBondSectorFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

        doCallRealMethod().when(sut).requestMapper(any());
        //ACT
        final FixedIncomeQuery result = sut.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(result).fixedIncomeSecuritiesAllocation(any());
        verify(result).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorFixedIncomeEndpoint sut = mock(FixedIncomeBondSectorFixedIncomeEndpoint.class);

            final FixedIncome fixedIncome = mock(FixedIncome.class);
            final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);
            when(fixedIncome.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
            final FixedIncomeHolding h = mock(FixedIncomeHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(fixedIncome, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorFixedIncomeEndpoint sut = mock(FixedIncomeBondSectorFixedIncomeEndpoint.class);

            final FixedIncome fixedIncome = mock(FixedIncome.class);
            final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
            when(fixedIncome.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
            final FixedIncomeHolding h = mock(FixedIncomeHolding.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RFixedIncomeBondSecurities expected = sut.responseMapper(fixedIncome, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

}
