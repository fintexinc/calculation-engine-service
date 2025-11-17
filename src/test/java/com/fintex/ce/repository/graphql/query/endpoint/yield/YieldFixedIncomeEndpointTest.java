
package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RYield;
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

public class YieldFixedIncomeEndpointTest {

    @Test
    public void getGetFixedIncomeByBroadridgeAdpNumbers_isPresent() {
        //SETUP
        final YieldFixedIncomeEndpoint sut = new YieldFixedIncomeEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FixedIncome> expected = new ArrayList<>();

        when(q.getGetFixedIncomeByBroadridgeAdpNumbers()).thenReturn(expected);

        //ACT
        final Function<Query, List<FixedIncome>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    public void requestMapper_verify() {
        //SETUP
        final YieldFixedIncomeEndpoint sut = Mockito.mock(YieldFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.interestRate(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final FixedIncomeQuery actual = sut.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).interestRate(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    public void responseMapper_verify() {
        //SETUP
        final YieldFixedIncomeEndpoint sut = Mockito.mock(YieldFixedIncomeEndpoint.class);

        final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);
        final FloatDatapoint interestRate = mock(FloatDatapoint.class);
        final BigDecimal yieldValue = mock(BigDecimal.class);

        final FixedIncome entity = mock(FixedIncome.class);
        when(entity.getInterestRate()).thenReturn(interestRate);
        when(interestRate.getValue()).thenReturn(yieldValue);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RYield result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(yieldValue, result.getDividendYield());
    }

}
