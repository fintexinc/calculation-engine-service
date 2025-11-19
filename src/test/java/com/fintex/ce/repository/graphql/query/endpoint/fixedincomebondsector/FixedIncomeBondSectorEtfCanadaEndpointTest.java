package com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector;

import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FixedIncomeSectorAllocation;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
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

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeBondSectorEtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final FixedIncomeBondSectorEtfCanadaEndpoint fixedIncomeBondSectorEtfCanadaEndpoint = new FixedIncomeBondSectorEtfCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = fixedIncomeBondSectorEtfCanadaEndpoint.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final FixedIncomeBondSectorEtfCanadaEndpoint fixedIncomeBondSectorEtfCanadaEndpoint = mock(FixedIncomeBondSectorEtfCanadaEndpoint.class);

        final EtfQuery etfQuery = mock(EtfQuery.class);
        when(etfQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(etfQuery);
        when(etfQuery.ticker(any())).thenReturn(etfQuery);

        doCallRealMethod().when(fixedIncomeBondSectorEtfCanadaEndpoint).requestMapper(any());
        //ACT
        final EtfQuery actual = fixedIncomeBondSectorEtfCanadaEndpoint.requestMapper(etfQuery);

        //VERIFY
        verify(actual).fixedIncomeSecuritiesAllocation(any());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_verifyFixedIncomeBondSectorMapper() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(FixedIncomeBondSectorEtfCanadaEndpoint.class);

            final Etf etf = mock(Etf.class);
            final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);
            when(etf.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
            final EtfHolding h = mock(EtfHolding.class);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(etf, h);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
        }
    }

    @Test
    void responseMapper_checkResult() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(FixedIncomeBondSectorEtfCanadaEndpoint.class);

            final Etf etf = mock(Etf.class);
            final FixedIncomeSectorAllocation allocation = mock(FixedIncomeSectorAllocation.class);
            when(etf.getFixedIncomeSectorAllocation()).thenReturn(allocation);
            final EtfHolding h = mock(EtfHolding.class);

            final RFixedIncomeBondSecurities actual = mock(RFixedIncomeBondSecurities.class);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(actual);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            final RFixedIncomeBondSecurities expected = sut.responseMapper(etf, h);

            //VERIFY
            assertSame(expected, actual);
        }
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final FixedIncomeBondSectorEtfCanadaEndpoint fixedIncomeBondSectorEtfCanadaEndpoint = mock(FixedIncomeBondSectorEtfCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String tickets = "TICKETS";
        final List<String> equityIdentifiers = List.of(tickets);

        doCallRealMethod().when(fixedIncomeBondSectorEtfCanadaEndpoint).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = fixedIncomeBondSectorEtfCanadaEndpoint.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getCanadaEtfsByTickers(eq(equityIdentifiers), any());
    }

}
