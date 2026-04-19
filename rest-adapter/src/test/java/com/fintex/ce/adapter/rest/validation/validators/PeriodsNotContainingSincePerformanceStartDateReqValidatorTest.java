package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;

class PeriodsNotContainingSincePerformanceStartDateReqValidatorTest
    extends
      AbstractPeriodsNotContainingReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new PeriodsNotContainingSincePerformanceStartDateReqValidator();
  }

  @Override
  String disallowedPeriodName() {
    return "SINCE_PERFORMANCE_START_DATE";
  }

  @Override
  String expectedErrorCode() {
    return "TIME_INTERVAL_PERIOD_CONTAINS_SINCE_PSD";
  }
}
