package com.fintex.ce.repository.graphql.query.endpoint.averagemer;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.ManagementFeeDatapoint;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.smclient.graphql.DataProvider.EAGLE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

class AverageMERCanadaHedgeFundEndpointTest {

    @Test
    void getGetUsEtfsByTickers_isPresent() {
        //SETUP
        final AverageMERCanadaHedgeFundEndpoint m = new AverageMERCanadaHedgeFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<HedgeFund> expected = new ArrayList<>();

        when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<HedgeFund>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }


    @Test
    void requestMapper_verify() {
        //SETUP
        final AverageMERCanadaHedgeFundEndpoint m = mock(AverageMERCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);

        when(hedgeFundQuery.managementExpenseRatio(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.managementFee(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).managementExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION);
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final AverageMERCanadaHedgeFundEndpoint a = mock(AverageMERCanadaHedgeFundEndpoint.class);

        final HedgeFund hedgeFund = mock(HedgeFund.class);
        final FloatDatapoint managementExpenseFloatDatapoint = mock(FloatDatapoint.class);
        when(hedgeFund.getManagementExpenseRatio()).thenReturn(managementExpenseFloatDatapoint);
        when(managementExpenseFloatDatapoint.getValue()).thenReturn(BigDecimal.ONE);
        when(managementExpenseFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

        final ManagementFeeDatapoint managementFloatDatapoint = mock(ManagementFeeDatapoint.class);
        when(managementFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
        when(hedgeFund.getManagementFee()).thenReturn(managementFloatDatapoint);
        when(managementFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

        final var expected = new RAverageMer();
        expected.setMer(managementExpenseFloatDatapoint.getValue());
        expected.setActualManagementFee(managementFloatDatapoint.getValue());
        expected.setMerProvider(DataProvider.EAGLE);
        expected.setActualManagementFeeProvider(DataProvider.EAGLE);

        doCallRealMethod().when(a).responseMapper(any(), any());
        //ACT
        final var actual = a.responseMapper(hedgeFund, null);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void responseMapper_checkResultNull() {
        //SETUP
        final var sut = mock(AverageMERCanadaHedgeFundEndpoint.class);

        final HedgeFund hedgeFund = mock(HedgeFund.class);
        final ManagementFeeDatapoint managementFloatDatapoint = mock(ManagementFeeDatapoint.class);
        when(managementFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
        when(hedgeFund.getManagementFee()).thenReturn(managementFloatDatapoint);
        when(managementFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

        final RAverageMer expected = new RAverageMer();
        expected.setMer(null);
        expected.setActualManagementFee(managementFloatDatapoint.getValue());
        expected.setProvider("");
        expected.setActualManagementFeeProvider(DataProvider.EAGLE);

        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        final var actual = sut.responseMapper(hedgeFund, null);

        //VERIFY

        assertEquals(expected, actual);
    }

}
