package com.fintex.ce.repository.graphql.query.endpoint.countryexposure;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.enumeration.HoldingType.FIXED_INCOME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CountryExposureFixedIncomeEndpointTest {

    CountryExposureFixedIncomeEndpoint countryExposureFixedIncomeEndpoint;

    @BeforeEach
    void setUp() {
        countryExposureFixedIncomeEndpoint = mock(CountryExposureFixedIncomeEndpoint.class);
    }

    @Test
    void getFixedIncomeByBroadridgeAdpNumbers_isPresent() {
        //SETUP
        countryExposureFixedIncomeEndpoint = new CountryExposureFixedIncomeEndpoint();
        final Query q = mock(Query.class);
        final ArrayList<FixedIncome> expected = new ArrayList<>();
        when(q.getGetFixedIncomeByBroadridgeAdpNumbers()).thenReturn(expected);

        //ACT
        final Function<Query, List<FixedIncome>> actual = countryExposureFixedIncomeEndpoint.getGetFDSEntityFunction();

        //VERIFY
        assertEquals(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
        when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);
        when(fixedIncomeQuery.countryAllocation(any())).thenReturn(fixedIncomeQuery);
        doCallRealMethod().when(countryExposureFixedIncomeEndpoint).requestMapper(any());

        //ACT
        final FixedIncomeQuery actual = countryExposureFixedIncomeEndpoint.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).externalIdentifiers(any());
        verify(actual).countryAllocation(any());
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final QueryQuery qq = mock(QueryQuery.class);
        final String fixedIncomeIdentifier = "fixedIncomeIdentifier";
        final UnaryOperator func = mock(UnaryOperator.class);
        when(func.apply(any())).thenReturn(mock(Object.class));
        doCallRealMethod().when(countryExposureFixedIncomeEndpoint).queryDefinition(any(), any());

        //ACT
        final QueryQueryDefinition actual = countryExposureFixedIncomeEndpoint.queryDefinition(List.of(fixedIncomeIdentifier), func);

        actual.define(qq);

        //VERIFY
        verify(qq).getFixedIncomeByBroadridgeAdpNumbers(eq(List.of(fixedIncomeIdentifier)), any());
    }

    @Test
    void responseMapper_verifyCountryExposureMapper() {
        //SETUP
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);
            final FixedIncome entity = mock(FixedIncome.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            when(entity.getCountryAllocation()).thenReturn(country);
            doCallRealMethod().when(countryExposureFixedIncomeEndpoint).responseMapper(any(), any());

            //ACT
            countryExposureFixedIncomeEndpoint.responseMapper(entity, holding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.countryExposureMapper(country));
        }
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            final FixedIncome entity = mock(FixedIncome.class);
            final CountryAllocation country = mock(CountryAllocation.class);
            final FixedIncomeHolding holding = mock(FixedIncomeHolding.class);
            final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
            when(entity.getCountryAllocation()).thenReturn(country);
            when(holding.getType()).thenReturn(FIXED_INCOME);
            mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.countryExposureMapper(any())).thenReturn(asset);
            doCallRealMethod().when(countryExposureFixedIncomeEndpoint).responseMapper(any(), any());

            //ACT
            final RCountryExposure actual = countryExposureFixedIncomeEndpoint.responseMapper(entity, holding);

            //VERIFY
            assertEquals(new RCountryExposure(holding.getType(), asset), actual);
        }
    }

}