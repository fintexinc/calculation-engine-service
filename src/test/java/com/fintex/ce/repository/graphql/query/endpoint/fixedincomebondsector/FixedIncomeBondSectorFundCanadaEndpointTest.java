package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeBondSectorFundCanadaEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final FixedIncomeBondSectorFundCanadaEndpoint fixedIncomeBondSectorFundCanadaEndpoint = new FixedIncomeBondSectorFundCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = fixedIncomeBondSectorFundCanadaEndpoint.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final FixedIncomeBondSectorFundCanadaEndpoint fixedIncomeBondSectorFundCanadaEndpoint = mock(FixedIncomeBondSectorFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(fixedIncomeBondSectorFundCanadaEndpoint).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = fixedIncomeBondSectorFundCanadaEndpoint.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).fixedIncomeSecuritiesAllocation(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyEquitySectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(FixedIncomeBondSectorFundCanadaEndpoint.class);

            final FundSeries fundSeries = mock(FundSeries.class);
            final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);
            when(fundSeries.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
            final FundSeriesHolding h = mock(FundSeriesHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(fundSeries, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(FixedIncomeBondSectorFundCanadaEndpoint.class);

            final FundSeries fundSeries = mock(FundSeries.class);
            final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
            when(fundSeries.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
            final FundSeriesHolding h = mock(FundSeriesHolding.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RFixedIncomeBondSecurities expected = sut.responseMapper(fundSeries, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final FixedIncomeBondSectorFundCanadaEndpoint fixedIncomeBondSectorFundCanadaEndpoint = mock(FixedIncomeBondSectorFundCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);
        final FundHoldingIdentifiersCodes fundHoldingIdentifiersCodes = mock(FundHoldingIdentifiersCodes.class);

        final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(fundHoldingIdentifiersCodes);

        doCallRealMethod().when(fixedIncomeBondSectorFundCanadaEndpoint).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = fixedIncomeBondSectorFundCanadaEndpoint.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
    }

}
