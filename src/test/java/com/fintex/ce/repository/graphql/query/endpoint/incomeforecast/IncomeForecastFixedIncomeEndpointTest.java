package com.fintex.ce.repository.graphql.query.endpoint.incomeforecast;

import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.smclient.graphql.StringsDatapoint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncomeForecastFixedIncomeEndpointTest {

    @Test
    void getGetFixedIncomeByBroadridgeAdpNumbers_isPresent() {
        //SETUP
        final IncomeForecastFixedIncomeEndpoint sut = new IncomeForecastFixedIncomeEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FixedIncome> expected = new ArrayList<>();

        when(q.getGetFixedIncomeByBroadridgeAdpNumbers()).thenReturn(expected);

        //ACT
        final Function<Query, List<FixedIncome>> actual = sut.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final IncomeForecastFixedIncomeEndpoint sut = Mockito.mock(IncomeForecastFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.interestRate(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.distributionDates(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.maturityDate(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.issueDate(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.paymentFrequency()).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final FixedIncomeQuery actual = sut.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).interestRate(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).maturityDate(STRING_DATAPOINT_QUERY_DEFINITION);
        verify(actual).issueDate(STRING_DATAPOINT_QUERY_DEFINITION);
        verify(actual).paymentFrequency();
        verify(actual).distributionDates(any());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        //SETUP
        final IncomeForecastFixedIncomeEndpoint sut = Mockito.mock(IncomeForecastFixedIncomeEndpoint.class);

        final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);
        final FloatDatapoint interestRate = mock(FloatDatapoint.class);
        final StringDatapoint maturityDate = mock(StringDatapoint.class);
        final StringDatapoint issueDate = mock(StringDatapoint.class);
        final StringsDatapoint distributionDates = mock(StringsDatapoint.class);
        final BigDecimal yieldValue = mock(BigDecimal.class);
        final List<String> schedule = mock(List.class);
        final String issueDateValue = "12-12-2020";
        final String maturityDateValue = "12-12-2023";

        final FixedIncome entity = mock(FixedIncome.class);
        when(entity.getInterestRate()).thenReturn(interestRate);
        when(interestRate.getValue()).thenReturn(yieldValue);
        when(entity.getDistributionDates()).thenReturn(distributionDates);
        when(entity.getIssueDate()).thenReturn(issueDate);
        when(entity.getMaturityDate()).thenReturn(maturityDate);
        when(distributionDates.getValues()).thenReturn(schedule);
        when(maturityDate.getValue()).thenReturn(maturityDateValue);
        when(issueDate.getValue()).thenReturn(issueDateValue);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RIncomeForecast result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(yieldValue, result.getDividendYield());
        Assertions.assertEquals(schedule, result.getSchedule());
        Assertions.assertEquals(issueDateValue, result.getIssueDate());
        Assertions.assertEquals(maturityDateValue, result.getMaturityDate());
    }

}
