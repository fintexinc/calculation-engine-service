package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_TIP_007;
import static com.fintex.ce.domain.model.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.domain.model.enumeration.Period.SINCE_PERFORMANCE_START_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodsNotContainingSincePerformanceStartDateReqValidationTest {

  @Test
  void check_periodsContainSInceCustomIntervalPerformanceDate() {
    // SETUP
    final var sut = new PeriodsNotContainingSincePerformanceStartDateReqValidation(
        Set.of(SINCE_PERFORMANCE_START_DATE.name(), SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()));

    final ReqValidationException expected = ERR_RRC_TIP_007.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

}