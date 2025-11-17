package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AverageMEREtfUsEndpointTest {

    @Test
    void getGetUsEtfsByTickers_isPresent() {
        //SETUP
        final AverageMEREtfUsEndpoint m = new AverageMEREtfUsEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetUsEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final AverageMEREtfUsEndpoint m = mock(AverageMEREtfUsEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final List<String> equityIdentifiers = List.of("TEST");

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getUsEtfsByTickers(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final AverageMEREtfUsEndpoint m = mock(AverageMEREtfUsEndpoint.class);

        final EtfQuery etfQuery = mock(EtfQuery.class);
        when(etfQuery.netExpenseRatio(any())).thenReturn(etfQuery);
        when(etfQuery.grossExpenseRatio(any())).thenReturn(etfQuery);
        when(etfQuery.ticker(any())).thenReturn(etfQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(etfQuery);

        //VERIFY
        verify(actual).netExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).grossExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final var sut = mock(AverageMEREtfUsEndpoint.class);

        final Etf etf = mock(Etf.class);
        final FloatDatapoint netFloatDatapoint = mock(FloatDatapoint.class);
        final FloatDatapoint grossFloatDatapoint = mock(FloatDatapoint.class);
        final var expected = new RAverageMer();
        expected.setNetExpenseRatio(BigDecimal.ONE);
        expected.setGrossExpenseRatio(BigDecimal.TEN);
        expected.setNetExpenseRatioProvider(com.fintex.ce.config.enumeration.DataProvider.EAGLE);
        expected.setGrossExpenseRatioProvider(com.fintex.ce.config.enumeration.DataProvider.EAGLE);

        when(netFloatDatapoint.getDataProvider()).thenReturn(DataProvider.EAGLE);
        when(grossFloatDatapoint.getDataProvider()).thenReturn(DataProvider.EAGLE);
        when(netFloatDatapoint.getValue()).thenReturn(BigDecimal.ONE);
        when(etf.getNetExpenseRatio()).thenReturn(netFloatDatapoint);
        when(grossFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
        when(etf.getGrossExpenseRatio()).thenReturn(grossFloatDatapoint);

        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        final var actual = sut.responseMapper(etf, null);

        //VERIFY
        assertEquals(expected, actual);
    }

}