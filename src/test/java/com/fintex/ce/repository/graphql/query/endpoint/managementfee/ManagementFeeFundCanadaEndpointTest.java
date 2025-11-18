package com.fintex.ce.repository.graphql.query.endpoint.managementfee;

import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.ManagementFeeDatapoint;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagementFeeFundCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final ManagementFeeFundCanadaEndpoint m = new ManagementFeeFundCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final ManagementFeeFundCanadaEndpoint m = mock(ManagementFeeFundCanadaEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.managementFee(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).managementFee(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final ManagementFeeFundCanadaEndpoint sut = mock(ManagementFeeFundCanadaEndpoint.class);

            final FundSeriesHolding holding = mock(FundSeriesHolding.class);

            final FundSeries entity = mock(FundSeries.class);
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
