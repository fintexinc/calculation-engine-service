package com.fintex.ce.repository.graphql.query.endpoint.creditquality;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RCreditQuality;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditQualityFixedIncomeEndpointTest {

    @Test
    void requestMapper_verify() {
        //SETUP
        final CreditQualityFixedIncomeEndpoint m = mock(CreditQualityFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.creditRating(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final FixedIncomeQuery actual = m.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).creditRating(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_verifyResult() {
        //SETUP
        final CreditQualityFixedIncomeEndpoint sut = mock(CreditQualityFixedIncomeEndpoint.class);

        final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);

        final FixedIncome entity = mock(FixedIncome.class);
        final StringDatapoint rating = mock(StringDatapoint.class);
        when(entity.getCreditRating()).thenReturn(rating);
        when(rating.getValue()).thenReturn("AAA");

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RCreditQuality result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getRatings());
        Assertions.assertEquals(1, result.getRatings().size());
        Assertions.assertTrue(result.getRatings().containsKey("AAA"));
        Assertions.assertEquals(BigDecimal.ONE, result.getRatings().get("AAA"));
    }

    @Test
    void responseMapper_verifyWithNullResponseFromFas() {
        //SETUP
        final CreditQualityFixedIncomeEndpoint sut = mock(CreditQualityFixedIncomeEndpoint.class);

        final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);

        final FixedIncome entity = mock(FixedIncome.class);
        when(entity.getCreditRating()).thenReturn(null);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RCreditQuality result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getRatings());
        Assertions.assertEquals(0, result.getRatings().size());
    }

}
