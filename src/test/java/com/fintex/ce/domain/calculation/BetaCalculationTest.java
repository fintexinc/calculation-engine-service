package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.response.BetaResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BetaCalculationTest {

    @Test
    void defineResponseType_verifyFormTimeIntervalResDTO() {
        //SETUP
        final BetaCalculation alpha = mock(BetaCalculation.class);

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
        final BetaCalculation beta = mock(BetaCalculation.class);

        final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2020-01-05", BigDecimal.ONE), Pair.of("2000-01-12", ZERO));

        final Set<TimeIntervalResDTO> expected = Set.of(
                new TimeIntervalResDTO("2000-01-12", ZERO),
                new TimeIntervalResDTO("2020-01-05", BigDecimal.ONE)
        );
        when(beta.formTimeIntervalResDTO(anySet())).thenReturn(expected);

        doCallRealMethod().when(beta).defineResponseType(anySet());
        //ACT
        final BetaResDTO actual = beta.defineResponseType(pairs);

        //VERIFY
        assertEquals(expected, actual.getBeta());
    }

}
