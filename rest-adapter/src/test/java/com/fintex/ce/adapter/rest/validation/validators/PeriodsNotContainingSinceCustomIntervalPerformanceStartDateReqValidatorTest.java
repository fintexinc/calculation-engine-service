package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;

class PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidatorTest
    extends
      AbstractPeriodsNotContainingReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new PeriodsNotContainingSinceCustomIntervalPerformanceStartDateReqValidator();
  }

  @Override
  String disallowedPeriodName() {
    return "SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE";
  }

  @Override
  String expectedErrorCode() {
    return "TIME_INTERVAL_PERIOD_CONTAINS_SINCE_CIPSD";
  }
}
