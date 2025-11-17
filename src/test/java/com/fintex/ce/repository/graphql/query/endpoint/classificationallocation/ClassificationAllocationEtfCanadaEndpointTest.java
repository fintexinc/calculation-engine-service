package com.fintex.ce.repository.graphql.query.endpoint.classificationallocation;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.SecurityClassification;
import com.fintex.smclient.graphql.SecurityClassificationAllocation;
import com.fintex.smclient.graphql.SecurityClassificationLevelOne;
import com.fintex.smclient.graphql.SecurityClassificationLevelThree;
import com.fintex.smclient.graphql.SecurityClassificationLevelTwo;
import com.fintex.smclient.graphql.SecurityClassificationTypeValue;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RClassificationAllocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassificationAllocationEtfCanadaEndpointTest {

    @Test
    void getGetCanadaEtfsByTickers_isPresent() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = new ClassificationAllocationEtfCanadaEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = mock(ClassificationAllocationEtfCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final List<String> equityIdentifiers = List.of("TEST");

        doCallRealMethod().when(sut).queryDefinition(any(), any());

        //ACT
        final QueryQueryDefinition actual = sut.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getCanadaEtfsByTickers(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = mock(ClassificationAllocationEtfCanadaEndpoint.class);

        final EtfQuery etfQuery = mock(EtfQuery.class);
        when(etfQuery.securityClassification(any())).thenReturn(etfQuery);
        when(etfQuery.securityClassificationAllocation(any())).thenReturn(etfQuery);
        when(etfQuery.ticker(any())).thenReturn(etfQuery);
        when(sut.loadProviders()).thenReturn(List.of());

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final EtfQuery actual = sut.requestMapper(etfQuery);

        //VERIFY
        verify(actual).securityClassificationAllocation(ClassificationAllocationEndpointUtil.getSecurityClassificationAllocationQueryDefinition());
        verify(actual).securityClassification(ClassificationAllocationEndpointUtil.getSecurityClassificationQueryDefinition());
        verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
    }

    @Test
    void responseMapper_verifyWithSecurityClassificationAllocationData() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = mock(ClassificationAllocationEtfCanadaEndpoint.class);

        final Etf etf = mock(Etf.class);
        final EtfHolding etfHolding = mock(EtfHolding.class);
        final SecurityClassificationAllocation securityClassificationAllocation = mock(SecurityClassificationAllocation.class);
        final SecurityClassification securityClassification = mock(SecurityClassification.class);
        final SecurityClassificationTypeValue securityClassificationTypeValue1 = mock(SecurityClassificationTypeValue.class);
        final SecurityClassificationTypeValue securityClassificationTypeValue2 = mock(SecurityClassificationTypeValue.class);

        when(etf.getSecurityClassificationAllocation()).thenReturn(securityClassificationAllocation);
        when(etf.getSecurityClassification()).thenReturn(securityClassification);
        when(securityClassificationAllocation.getValues()).thenReturn(List.of(securityClassificationTypeValue1, securityClassificationTypeValue2));
        when(securityClassificationTypeValue1.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
        when(securityClassificationTypeValue1.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.CANADA);
        when(securityClassificationTypeValue1.getValue()).thenReturn(new BigDecimal("0.98"));
        when(securityClassificationTypeValue2.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
        when(securityClassificationTypeValue2.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.US);
        when(securityClassificationTypeValue2.getValue()).thenReturn(new BigDecimal("0.02"));

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RClassificationAllocation result = sut.responseMapper(etf, etfHolding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getSecurityClassificationValues().size());
        Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__CANADA"));
        Assertions.assertEquals(new BigDecimal("0.98"), result.getSecurityClassificationValues().get("EQUITY__CANADA"));
        Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__US"));
        Assertions.assertEquals(new BigDecimal("0.02"), result.getSecurityClassificationValues().get("EQUITY__US"));
    }

    @Test
    void responseMapper_verifyWithSecurityClassificationData() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = mock(ClassificationAllocationEtfCanadaEndpoint.class);

        final Etf etf = mock(Etf.class);
        final EtfHolding etfHolding = mock(EtfHolding.class);
        final SecurityClassification securityClassification = mock(SecurityClassification.class);

        when(etf.getSecurityClassificationAllocation()).thenReturn(null);
        when(etf.getSecurityClassification()).thenReturn(securityClassification);
        when(securityClassification.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
        when(securityClassification.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.CANADA);
        when(securityClassification.getLevelThree()).thenReturn(SecurityClassificationLevelThree.UNCLASSIFIED);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RClassificationAllocation result = sut.responseMapper(etf, etfHolding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getSecurityClassificationValues().size());
        Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__CANADA"));
        Assertions.assertEquals(BigDecimal.ONE, result.getSecurityClassificationValues().get("EQUITY__CANADA"));
    }

    @Test
    void responseMapper_verifyWithoutSecurityClassificationData() {
        //SETUP
        final ClassificationAllocationEtfCanadaEndpoint sut = mock(ClassificationAllocationEtfCanadaEndpoint.class);

        final Etf etf = mock(Etf.class);
        final EtfHolding etfHolding = mock(EtfHolding.class);

        when(etf.getSecurityClassificationAllocation()).thenReturn(null);
        when(etf.getSecurityClassification()).thenReturn(null);

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RClassificationAllocation result = sut.responseMapper(etf, etfHolding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getSecurityClassificationValues().isEmpty());
    }

}
