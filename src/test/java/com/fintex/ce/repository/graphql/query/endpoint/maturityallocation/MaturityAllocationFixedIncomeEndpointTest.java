package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringDatapoint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationFixedIncomeEndpointTest {

    private MaturityAllocationFixedIncomeEndpoint maturityAllocationFixedIncomeEndpoint;
    private FixedIncome fixedIncome;
    private FixedIncomeHolding fixedIncomeHolding;

    private static final LocalDate CURRENT_DATE = new LocalDate("2024-01-01");
    private static final String MATURITY_DATE_KEY = "maturityDate";
    private static final String RESULT_KEY = "result";

    @BeforeEach
    void setUp() {
        maturityAllocationFixedIncomeEndpoint = mock(MaturityAllocationFixedIncomeEndpoint.class);
        fixedIncome = mock(FixedIncome.class);
        fixedIncomeHolding = mock(FixedIncomeHolding.class);
    }

    @Test
    void getFixedIncomeByBroadridgeId_isPresent() {
        //SETUP
        maturityAllocationFixedIncomeEndpoint = new MaturityAllocationFixedIncomeEndpoint();
        final Query query = mock(Query.class);
        final ArrayList<FixedIncome> expected = new ArrayList<>();

        when(query.getGetFixedIncomeByBroadridgeAdpNumbers()).thenReturn(expected);

        //ACT
        final Function<Query, List<FixedIncome>> actual = maturityAllocationFixedIncomeEndpoint.getGetSMEntityFunction();

        //VERIFY
        assertEquals(expected, actual.apply(query));
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);

        when(fixedIncomeQuery.maturityDate(any())).thenReturn(fixedIncomeQuery);
        doCallRealMethod().when(maturityAllocationFixedIncomeEndpoint).requestMapper(any());

        //ACT
        final FixedIncomeQuery actual = maturityAllocationFixedIncomeEndpoint.requestMapper(fixedIncomeQuery);

        //VERIFY
        verify(actual).maturityDate(any());
    }

    @ParameterizedTest
    @MethodSource("getMaturityDateArguments")
    void responseMapper_verify(String maturityDate, MaturityAllocationType maturityAllocationType) {
        //SETUP
        doCallRealMethod().when(maturityAllocationFixedIncomeEndpoint).responseMapper(any(), any());
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(CURRENT_DATE);
            final StringDatapoint maturityDateStringDatapoint = new StringDatapoint();
            maturityDateStringDatapoint.setValue(maturityDate);
            when(fixedIncome.getMaturityDate()).thenReturn(maturityDateStringDatapoint);

            //ACT
            final RMaturityAllocation result = maturityAllocationFixedIncomeEndpoint.responseMapper(fixedIncome, fixedIncomeHolding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getMaturityDurationValues());
            assertEquals(1, result.getMaturityDurationValues().size());
            final Map.Entry<String, BigDecimal> entry = result.getMaturityDurationValues().entrySet().stream().findFirst().orElseThrow();
            assertEquals(maturityAllocationType.name(), entry.getKey());
            assertEquals(BigDecimal.ONE, entry.getValue());
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-12-30", "2023-12-31"})
    void responseMapper_verify_passedMaturityDate(String maturityDate) {
        //SETUP
        doCallRealMethod().when(maturityAllocationFixedIncomeEndpoint).responseMapper(any(), any());

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(CURRENT_DATE);
            final StringDatapoint maturityDateStringDatapoint = new StringDatapoint();
            maturityDateStringDatapoint.setValue(maturityDate);
            when(fixedIncome.getMaturityDate()).thenReturn(maturityDateStringDatapoint);

            //ACT
            final RMaturityAllocation result = maturityAllocationFixedIncomeEndpoint.responseMapper(fixedIncome, fixedIncomeHolding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getMaturityDurationValues());
            assertEquals(0, result.getMaturityDurationValues().size());;
        }
    }

    @Test
    void responseMapper_verify_nullMaturityDate() {
        //SETUP
        when(fixedIncome.getMaturityDate()).thenReturn(null);
        doCallRealMethod().when(maturityAllocationFixedIncomeEndpoint).responseMapper(any(), any());
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(CURRENT_DATE);

            //ACT
            final RMaturityAllocation result = maturityAllocationFixedIncomeEndpoint.responseMapper(fixedIncome, fixedIncomeHolding);

            //VERIFY
            assertNotNull(result);
            assertNotNull(result.getMaturityDurationValues());
            assertEquals(0, result.getMaturityDurationValues().size());
        }
    }

    private static final String[] MATURITY_DATES = {
            "2024-01-02", "2024-01-08", "2024-01-09", "2024-01-31", "2024-02-01",
            "2024-03-31", "2024-04-01", "2024-07-01", "2024-07-02", "2024-12-30",
            "2025-01-01", "2026-12-31", "2027-01-01", "2028-12-31", "2029-01-01",
            "2030-12-31", "2031-01-01", "2033-12-31", "2034-01-01", "2038-12-31",
            "2039-01-01", "2043-12-31", "2044-01-01", "2053-12-31", "2054-01-01"
    };

    private static final MaturityAllocationType[] MATURITY_TYPES = {
            MaturityAllocationType.ONE_TO_SEVEN_DAYS, MaturityAllocationType.ONE_TO_SEVEN_DAYS,
            MaturityAllocationType.EIGHT_TO_THIRTY_DAYS, MaturityAllocationType.EIGHT_TO_THIRTY_DAYS,
            MaturityAllocationType.THIRTYONE_TO_NINTY_DAYS, MaturityAllocationType.THIRTYONE_TO_NINTY_DAYS,
            MaturityAllocationType.NINTYONE_TO_182_DAYS, MaturityAllocationType.NINTYONE_TO_182_DAYS,
            MaturityAllocationType.ONEHUNDREDANDEIGHTYTHREE_TO_364_DAYS, MaturityAllocationType.ONEHUNDREDANDEIGHTYTHREE_TO_364_DAYS,
            MaturityAllocationType.ONE_TO_THREE_YEARS, MaturityAllocationType.ONE_TO_THREE_YEARS,
            MaturityAllocationType.THREE_TO_FIVE_YEARS, MaturityAllocationType.THREE_TO_FIVE_YEARS,
            MaturityAllocationType.FIVE_TO_SEVEN_YEARS, MaturityAllocationType.FIVE_TO_SEVEN_YEARS,
            MaturityAllocationType.SEVEN_TO_TEN_YEARS, MaturityAllocationType.SEVEN_TO_TEN_YEARS,
            MaturityAllocationType.TEN_TO_FIFTEEN_YEARS, MaturityAllocationType.TEN_TO_FIFTEEN_YEARS,
            MaturityAllocationType.FIFTEEN_TO_TWENTY_YEARS, MaturityAllocationType.FIFTEEN_TO_TWENTY_YEARS,
            MaturityAllocationType.TWENTY_TO_THIRTY_YEARS, MaturityAllocationType.TWENTY_TO_THIRTY_YEARS,
            MaturityAllocationType.MORE_THAN_THIRTY_YEARS
    };

    private static Stream<Arguments> getMaturityDateArguments() {
        return IntStream.range(0, MATURITY_TYPES.length)
                .mapToObj(i -> Arguments.of(MATURITY_DATES[i], MATURITY_TYPES[i]));
    }

}
