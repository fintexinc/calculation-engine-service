package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.ManagementFeeDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagementFeeUsMutualFundEndpointTest {

    @Test
    void getGetBy_isPresent() {
        //SETUP
        final ManagementFeeUsMutualFundEndpoint m = new ManagementFeeUsMutualFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<UsFund> expected = new ArrayList<>();

        when(q.getGetUsFundsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<UsFund>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final ManagementFeeUsMutualFundEndpoint m = mock(ManagementFeeUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.managementFee(any())).thenReturn(usFundQuery);
        when(usFundQuery.ticker(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final UsFundQuery actual = m.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).managementFee(any());
        verify(actual).ticker(any());
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final ManagementFeeUsMutualFundEndpoint sut = mock(ManagementFeeUsMutualFundEndpoint.class);

            final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

            final UsFund entity = mock(UsFund.class);
            final BigDecimal value = mock(BigDecimal.class);
            final ManagementFeeDatapoint managementFeeDatapoint = mock(ManagementFeeDatapoint.class);
            when(entity.getManagementFee()).thenReturn(managementFeeDatapoint);
            when(managementFeeDatapoint.getValue()).thenReturn(value);
            when(managementFeeDatapoint.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

            doCallRealMethod().when(sut).responseMapper(any(), any());

            //ACT
            final RManagementFee result = sut.responseMapper(entity, holding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getManagementFee());
            assertEquals(value, result.getManagementFee());
            assertEquals(DataProvider.MORNINGSTAR.name(), result.getProvider());

        }
    }
    
}
