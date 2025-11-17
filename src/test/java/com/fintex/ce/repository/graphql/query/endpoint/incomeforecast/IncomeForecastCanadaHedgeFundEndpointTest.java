package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncomeForecastCanadaHedgeFundEndpointTest {

    @Test
    void getGetCanadaHedgeFundsByMorningstarIds_isPresent() {
        //SETUP
        final IncomeForecastCanadaHedgeFundEndpoint sut = new IncomeForecastCanadaHedgeFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<HedgeFund> expected = new ArrayList<>();

        when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<HedgeFund>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final IncomeForecastCanadaHedgeFundEndpoint sut = Mockito.mock(IncomeForecastCanadaHedgeFundEndpoint.class);

        final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
        when(hedgeFundQuery.dividendYield(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.distributionDates(any())).thenReturn(hedgeFundQuery);
        when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final HedgeFundQuery actual = sut.requestMapper(hedgeFundQuery);

        //VERIFY
        verify(actual).dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).distributionDates(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        //SETUP
        final IncomeForecastCanadaHedgeFundEndpoint sut = Mockito.mock(IncomeForecastCanadaHedgeFundEndpoint.class);

        final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);
        final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
        final StringsDatapoint distributionDates = mock(StringsDatapoint.class);
        final BigDecimal yieldValue = mock(BigDecimal.class);
        final List<String> schedule = mock(List.class);

        final HedgeFund entity = mock(HedgeFund.class);
        when(entity.getDividendYield()).thenReturn(dividendYield);
        when(dividendYield.getValue()).thenReturn(yieldValue);
        when(entity.getDistributionDates()).thenReturn(distributionDates);
        when(distributionDates.getValues()).thenReturn(schedule);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RIncomeForecast result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(yieldValue, result.getDividendYield());
        Assertions.assertEquals(schedule, result.getSchedule());
    }

}
