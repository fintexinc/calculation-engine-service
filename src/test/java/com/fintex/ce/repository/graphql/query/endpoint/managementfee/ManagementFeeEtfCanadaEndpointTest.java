package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagementFeeEtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final ManagementFeeEtfCanadaEndpoint m = new ManagementFeeEtfCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final ManagementFeeEtfCanadaEndpoint m = mock(ManagementFeeEtfCanadaEndpoint.class);

        final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
        when(fundSeriesQuery.managementFee(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).managementFee(any());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final ManagementFeeEtfCanadaEndpoint sut = mock(ManagementFeeEtfCanadaEndpoint.class);

            final EtfHolding holding = mock(EtfHolding.class);

            final Etf entity = mock(Etf.class);
            final BigDecimal value = mock(BigDecimal.class);
            final FloatDatapoint managementFeeDatapoint = mock(FloatDatapoint.class);
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
