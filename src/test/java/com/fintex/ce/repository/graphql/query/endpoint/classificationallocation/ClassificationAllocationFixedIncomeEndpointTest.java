package com.fintex.ce.repository.graphql.query.endpoint.classificationallocation;

import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.SecurityClassification;
import com.fintex.smclient.graphql.SecurityClassificationLevelOne;
import com.fintex.smclient.graphql.SecurityClassificationLevelThree;
import com.fintex.smclient.graphql.SecurityClassificationLevelTwo;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RClassificationAllocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassificationAllocationFixedIncomeEndpointTest {

    @Test
    void getGetFixedIncomeByTickersAndExchangeIds_isPresent() {
        //SETUP
        final ClassificationAllocationFixedIncomeEndpoint sut = new ClassificationAllocationFixedIncomeEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FixedIncome> expected = new ArrayList<>();

        when(q.getGetFixedIncomeByBroadridgeAdpNumbers()).thenReturn(expected);

        //ACT
        final Function<Query, List<FixedIncome>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final ClassificationAllocationFixedIncomeEndpoint sut = mock(ClassificationAllocationFixedIncomeEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);
        final String fixedIncomeIdentifiers = "fixedIncomeIdentifiers";


        doCallRealMethod().when(sut).queryDefinition(any(), any());

        //ACT
        final QueryQueryDefinition actual = sut.queryDefinition(List.of(fixedIncomeIdentifiers), mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getFixedIncomeByBroadridgeAdpNumbers(eq(List.of(fixedIncomeIdentifiers)), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final ClassificationAllocationFixedIncomeEndpoint sut = mock(ClassificationAllocationFixedIncomeEndpoint.class);

        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.securityClassification(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);
        when(sut.loadProviders()).thenReturn(List.of());

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final FixedIncomeQuery actual = sut.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition());
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verifyWithSecurityClassificationData() {
        //SETUP
        final ClassificationAllocationFixedIncomeEndpoint sut = mock(ClassificationAllocationFixedIncomeEndpoint.class);

        final FixedIncome fixedIncome = mock(FixedIncome.class);
        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
        final SecurityClassification securityClassification = mock(SecurityClassification.class);

        when(fixedIncome.getSecurityClassification()).thenReturn(securityClassification);
        when(securityClassification.getLevelOne()).thenReturn(SecurityClassificationLevelOne.FIXED_INCOME);
        when(securityClassification.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.CANADA);
        when(securityClassification.getLevelThree()).thenReturn(SecurityClassificationLevelThree.UNCLASSIFIED);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RClassificationAllocation result = sut.responseMapper(fixedIncome, fixedIncomeHolding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getSecurityClassificationValues().size());
        Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("FIXED_INCOME__CANADA"));
        Assertions.assertEquals(BigDecimal.ONE, result.getSecurityClassificationValues().get("FIXED_INCOME__CANADA"));
    }

    @Test
    void responseMapper_verifyWithoutSecurityClassificationData() {
        //SETUP
        final ClassificationAllocationFixedIncomeEndpoint sut = mock(ClassificationAllocationFixedIncomeEndpoint.class);

        final FixedIncome fixedIncome = mock(FixedIncome.class);
        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);

        when(fixedIncome.getSecurityClassification()).thenReturn(null);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RClassificationAllocation result = sut.responseMapper(fixedIncome, fixedIncomeHolding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getSecurityClassificationValues().isEmpty());
    }

}
