package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;

class PeriodsNotContainingYearToDateReqValidatorTest
    extends
      AbstractPeriodsNotContainingReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new PeriodsNotContainingYearToDateReqValidator();
  }

  @Override
  String disallowedPeriodName() {
    return "YEAR_TO_DATE";
  }

  @Override
  String expectedErrorCode() {
    return "ERR_RRC_TIP_002";
  }
}
