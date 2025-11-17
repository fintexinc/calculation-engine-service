package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.TrailingTotalReturnsReqValidator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TrailingTotalReturnsCalculationServiceImplImplTest {

    @Test
    void defineCalculationMethod_verifyBuildCalculationDto() {
        //SETUP
        final var requestValidator = mock(TrailingTotalReturnsReqValidator.class);
        final var monthlyReturnsService = mock(MonthlyReturnsService.class);
        final var set = mock(Set.class);
        final var sut = mock(TrailingTotalReturnsCalculationServiceImpl.class,
                withSettings().useConstructor(monthlyReturnsService, set, requestValidator));

        final PeriodsReqDTO req = mock(PeriodsReqDTO.class);

        when(sut.buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO)).thenReturn(new CalculationDTO());

        doCallRealMethod().when(sut).defineCalculationMethod(req);
        //ACT
        sut.defineCalculationMethod(req);

        //VERIFY
        verify(sut).buildCalculationDto(req, ReturnFactorScale.SCALE_OF_TWO);
    }

}