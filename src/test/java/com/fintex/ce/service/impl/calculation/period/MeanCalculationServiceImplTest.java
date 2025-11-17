package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.MeanCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_002;
import static com.fintex.ce.config.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MeanCalculationServiceImplTest {

    @Test
    void calculationSpecificChecks_checkResult() {
        //SETUP
        final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("10"));

        doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
        //ACT
        final ReqValidationException e = assertThrows(ReqValidationException.class, () -> meanCalculationService.addSpecificChecks(p));

        //VERIFY
        assertEquals(ERR_RRC_TIP_001.getMessage(), e.getMessage());
    }

    @Test
    void calculationSpecificChecks_checkResult2() {
        //SETUP
        final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

        doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
        //ACT
        final ReqValidationException e = assertThrows(ReqValidationException.class, () -> meanCalculationService.addSpecificChecks(p));

        //VERIFY
        assertEquals(ERR_RRC_TIP_002.getMessage(), e.getMessage());
    }

    @Test
    void calculationSpecificChecks_checkResult3() {
        //SETUP
        final MeanCalculationServiceImpl meanCalculationService = mock(MeanCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("12", "14", "22", "64", SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

        doCallRealMethod().when(meanCalculationService).addSpecificChecks(any());
        //ACT
        Assertions.assertDoesNotThrow(() -> meanCalculationService.addSpecificChecks(p));

        //VERIFY
    }


    @Test
    void defineCalculationMethod_checkResult() {
        //SETUP
        final var requestValidator = mock(PeriodsReqDtoValidator.class);
        final var sut = mock(MeanCalculationServiceImpl.class, withSettings()
                .useConstructor(null, Set.of("12", "36", "60", "120"), requestValidator));
        final var req = mock(PeriodsReqDTO.class);
        final var calculationDTO = mock(CalculationDTO.class);
        final var expected = new MeanCalculation<>(calculationDTO, Set.of("12", "36", "60", "120")).setScale(OUTPUT_SCALE);

        when(sut.buildCalculationDto(any(), any())).thenReturn(calculationDTO);

        doCallRealMethod().when(sut).defineCalculationMethod(any());
        //ACT
        final MeanCalculation actual = sut.defineCalculationMethod(req);

        //VERIFY
        assertEquals(expected, actual);
    }

}
