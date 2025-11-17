package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.smclient.graphql.DataProvider.EAGLE;
import static com.fintex.smclient.graphql.DataProvider.MORNINGSTAR;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

class AverageMEREtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final AverageMEREtfCanadaEndpoint m = new AverageMEREtfCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final AverageMEREtfCanadaEndpoint m = mock(AverageMEREtfCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final List<String> equityIdentifiers = List.of("TEST");

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getCanadaEtfsByTickers(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final AverageMEREtfCanadaEndpoint sut = mock(AverageMEREtfCanadaEndpoint.class);

        final EtfQuery etfQuery = mock(EtfQuery.class);
        when(etfQuery.managementExpenseRatio(any())).thenReturn(etfQuery);
        when(etfQuery.managementFee(any())).thenReturn(etfQuery);
        when(etfQuery.ticker(any())).thenReturn(etfQuery);
        when(sut.loadProviders()).thenReturn(List.of(EAGLE, MORNINGSTAR));

        doCallRealMethod().when(sut).requestMapper(any());
        //ACT
        final EtfQuery actual = sut.requestMapper(etfQuery);

        //VERIFY
        verify(actual).managementExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).managementFee(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_checkResultNull() {
        //SETUP
        final AverageMEREtfCanadaEndpoint sut = mock(AverageMEREtfCanadaEndpoint.class);

        final Etf etf = mock(Etf.class);
        final FloatDatapoint managementFloatDatapoint = mock(FloatDatapoint.class);
        when(managementFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
        when(etf.getManagementFee()).thenReturn(managementFloatDatapoint);
        when(managementFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

        final RAverageMer expected = new RAverageMer();
        expected.setMer(null);
        expected.setActualManagementFee(managementFloatDatapoint.getValue());
        expected.setProvider("");
        expected.setActualManagementFeeProvider(DataProvider.EAGLE);

        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        final var actual = sut.responseMapper(etf, null);

        //VERIFY

        assertEquals(expected, actual);
    }

}