package com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationEndpointUtil;
import com.fintex.ce.adapter.graphqlclient.endpoint.classificationallocation.ClassificationAllocationFundCanadaEndpoint;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.SecurityClassification;
import com.fintex.smclient.graphql.SecurityClassificationAllocation;
import com.fintex.smclient.graphql.SecurityClassificationLevelOne;
import com.fintex.smclient.graphql.SecurityClassificationLevelThree;
import com.fintex.smclient.graphql.SecurityClassificationLevelTwo;
import com.fintex.smclient.graphql.SecurityClassificationTypeValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassificationAllocationFundCanadaEndpointTest {

  @Test
  void getGetFundSeriesByHoldingCodes_isPresent() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = new ClassificationAllocationFundCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<FundSeries> expected = new ArrayList<>();

    when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

    // ACT
    final Function<Query, List<FundSeries>> actual = sut.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = mock(ClassificationAllocationFundCanadaEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);
    final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);

    doCallRealMethod().when(sut).queryDefinition(any(), any());

    // ACT
    final QueryQueryDefinition actual = sut.queryDefinition(List.of(codes), mock(UnaryOperator.class));
    actual.define(qq);

    // VERIFY
    verify(qq).getFundSeriesByHoldingCodes(eq(List.of(codes)), any());
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = mock(ClassificationAllocationFundCanadaEndpoint.class);

    final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
    when(fundSeriesQuery.securityClassification(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.securityClassificationAllocation(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);
    when(sut.loadProviders()).thenReturn(List.of());

    doCallRealMethod().when(sut).requestMapper(any());

    // ACT
    final FundSeriesQuery actual = sut.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).securityClassificationAllocation(ClassificationAllocationEndpointUtil
        .getSecurityClassificationAllocationQueryDefinition());
    verify(actual).securityClassification(ClassificationAllocationEndpointUtil
        .getSecurityClassificationQueryDefinition());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyWithSecurityClassificationAllocationData() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = mock(ClassificationAllocationFundCanadaEndpoint.class);

    final FundSeries fundSeries = mock(FundSeries.class);
    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
    final SecurityClassificationAllocation securityClassificationAllocation = mock(
        SecurityClassificationAllocation.class);
    final SecurityClassification securityClassification = mock(SecurityClassification.class);
    final SecurityClassificationTypeValue securityClassificationTypeValue1 = mock(
        SecurityClassificationTypeValue.class);
    final SecurityClassificationTypeValue securityClassificationTypeValue2 = mock(
        SecurityClassificationTypeValue.class);

    when(fundSeries.getSecurityClassificationAllocation()).thenReturn(securityClassificationAllocation);
    when(fundSeries.getSecurityClassification()).thenReturn(securityClassification);
    when(securityClassificationAllocation.getValues()).thenReturn(List.of(securityClassificationTypeValue1,
        securityClassificationTypeValue2));
    when(securityClassificationTypeValue1.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
    when(securityClassificationTypeValue1.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.CANADA);
    when(securityClassificationTypeValue1.getValue()).thenReturn(new BigDecimal("0.98"));
    when(securityClassificationTypeValue2.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
    when(securityClassificationTypeValue2.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.US);
    when(securityClassificationTypeValue2.getValue()).thenReturn(new BigDecimal("0.02"));

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final ClassificationAllocation result = sut.responseMapper(fundSeries, fundSeriesHolding);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(2, result.getSecurityClassificationValues().size());
    Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__CANADA"));
    Assertions.assertEquals(new BigDecimal("0.98"), result.getSecurityClassificationValues().get("EQUITY__CANADA"));
    Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__US"));
    Assertions.assertEquals(new BigDecimal("0.02"), result.getSecurityClassificationValues().get("EQUITY__US"));
  }

  @Test
  void responseMapper_verifyWithSecurityClassificationData() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = mock(ClassificationAllocationFundCanadaEndpoint.class);

    final FundSeries fundSeries = mock(FundSeries.class);
    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
    final SecurityClassification securityClassification = mock(SecurityClassification.class);

    when(fundSeries.getSecurityClassificationAllocation()).thenReturn(null);
    when(fundSeries.getSecurityClassification()).thenReturn(securityClassification);
    when(securityClassification.getLevelOne()).thenReturn(SecurityClassificationLevelOne.EQUITY);
    when(securityClassification.getLevelTwo()).thenReturn(SecurityClassificationLevelTwo.CANADA);
    when(securityClassification.getLevelThree()).thenReturn(SecurityClassificationLevelThree.UNCLASSIFIED);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final ClassificationAllocation result = sut.responseMapper(fundSeries, fundSeriesHolding);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getSecurityClassificationValues().size());
    Assertions.assertTrue(result.getSecurityClassificationValues().containsKey("EQUITY__CANADA"));
    Assertions.assertEquals(BigDecimal.ONE, result.getSecurityClassificationValues().get("EQUITY__CANADA"));
  }

  @Test
  void responseMapper_verifyWithoutSecurityClassificationData() {
    // SETUP
    final ClassificationAllocationFundCanadaEndpoint sut = mock(ClassificationAllocationFundCanadaEndpoint.class);

    final FundSeries fundSeries = mock(FundSeries.class);
    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);

    when(fundSeries.getSecurityClassificationAllocation()).thenReturn(null);
    when(fundSeries.getSecurityClassification()).thenReturn(null);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final ClassificationAllocation result = sut.responseMapper(fundSeries, fundSeriesHolding);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getSecurityClassificationValues());
  }

}
