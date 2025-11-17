package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.RSquaredResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class RSquaredCalculationTest {

    @Test
    void defineResponseType_verifyFormTimeIntervalResDTO() {
        //SETUP
        final RSquaredCalculation alpha = mock(RSquaredCalculation.class);

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05", BigDecimal.ONE));

        doCallRealMethod().when(alpha).defineResponseType(anySet());
        //ACT
        alpha.defineResponseType(pairs);

        //VERIFY
        verify(alpha).formTimeIntervalResDTO(pairs);
    }

    @Test
    void defineResponseType_checkResult() {
        //SETUP
        final RSquaredCalculation rSquaredCalculation = mock(RSquaredCalculation.class);

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2020-01-05", BigDecimal.ONE), Pair.of("2000-01-12", ZERO));

        final Set<TimeIntervalResDTO> expected = Set.of(
                new TimeIntervalResDTO("2000-01-12", ZERO),
                new TimeIntervalResDTO("2020-01-05", BigDecimal.ONE)
        );
        when(rSquaredCalculation.formTimeIntervalResDTO(anySet())).thenReturn(expected);

        doCallRealMethod().when(rSquaredCalculation).defineResponseType(anySet());
        //ACT
        final RSquaredResDTO actual = rSquaredCalculation.defineResponseType(pairs);

        //VERIFY
        assertEquals(expected, actual.getRSquared());
    }

}
