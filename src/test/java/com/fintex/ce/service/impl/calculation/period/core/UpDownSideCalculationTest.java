package com.fintex.ce.service.impl.calculation.period.core;

import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.service.impl.calculation.period.UpsideCaptureCalculationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_TIP_002;
import static com.fintex.ce.config.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UpDownSideCalculationTest {

    @Test
    void calculationSpecificChecks_checkResult() {
        //SETUP
        final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("11"));

        doCallRealMethod().when(u).addSpecificChecks(any());
        //ACT
        final ReqValidationException e = assertThrows(ReqValidationException.class, () -> u.addSpecificChecks(p));

        //VERIFY
        assertEquals(ERR_RRC_TIP_001.getMessage(), e.getMessage());
    }

    @Test
    void calculationSpecificChecks_checkResult2() {
        //SETUP
        final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("YEAR_TO_DATE"));

        doCallRealMethod().when(u).addSpecificChecks(any());
        //ACT
        final ReqValidationException e = assertThrows(ReqValidationException.class, () -> u.addSpecificChecks(p));

        //VERIFY
        assertEquals(ERR_RRC_TIP_002.getMessage(), e.getMessage());
    }

    @Test
    void calculationSpecificChecks_checkResult3() {
        //SETUP
        final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of("12", SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

        doCallRealMethod().when(u).addSpecificChecks(any());
        //ACT
        Assertions.assertDoesNotThrow(() -> u.addSpecificChecks(p));

        //VERIFY
    }

    @Test
    void calculationSpecificChecks_checkResult4() {
        //SETUP
        final UpsideCaptureCalculationServiceImpl u = mock(UpsideCaptureCalculationServiceImpl.class);

        final PeriodsReqDTO p = mock(PeriodsReqDTO.class);
        when(p.getPeriods()).thenReturn(Set.of());

        doCallRealMethod().when(u).addSpecificChecks(p);
        //ACT
        Assertions.assertDoesNotThrow(() -> u.addSpecificChecks(p));

        //VERIFY
    }

}