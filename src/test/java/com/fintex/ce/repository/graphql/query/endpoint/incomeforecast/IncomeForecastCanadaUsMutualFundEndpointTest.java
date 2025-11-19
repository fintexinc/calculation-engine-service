package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringsDatapoint;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncomeForecastCanadaUsMutualFundEndpointTest {

    @Test
    void getGetUsFundsByTickers_isPresent() {
        //SETUP
        final IncomeForecastCanadaUsMutualFundEndpoint sut = new IncomeForecastCanadaUsMutualFundEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<UsFund> expected = new ArrayList<>();

        when(q.getGetUsFundsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<UsFund>> actual = sut.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final IncomeForecastCanadaUsMutualFundEndpoint sut = Mockito.mock(IncomeForecastCanadaUsMutualFundEndpoint.class);

        final UsFundQuery usFundQuery = mock(UsFundQuery.class);
        when(usFundQuery.dividendYield(any())).thenReturn(usFundQuery);
        when(usFundQuery.distributionDates(any())).thenReturn(usFundQuery);
        when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final UsFundQuery actual = sut.requestMapper(usFundQuery);

        //VERIFY
        verify(actual).dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).distributionDates(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        //SETUP
        final IncomeForecastCanadaUsMutualFundEndpoint sut = Mockito.mock(IncomeForecastCanadaUsMutualFundEndpoint.class);

        final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);
        final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
        final StringsDatapoint distributionDates = mock(StringsDatapoint.class);
        final BigDecimal yieldValue = mock(BigDecimal.class);
        final List<String> schedule = mock(List.class);

        final UsFund entity = mock(UsFund.class);
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
