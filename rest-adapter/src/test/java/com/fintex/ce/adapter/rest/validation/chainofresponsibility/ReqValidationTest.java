package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.mockito.Mockito.*;

class ReqValidationTest {

  @Test
  void linkWith_addNewElementToValidations() {
    // SETUP
    final ReqValidation sut = mock(ReqValidation.class);
    final LinkedList validations = mock(LinkedList.class);
    sut.validations = validations;

    final NotNullReqValidation notNullReqValidation = mock(NotNullReqValidation.class);

    doCallRealMethod().when(sut).linkWith(any());
    // ACT
    sut.linkWith(notNullReqValidation);

    // VERIFY
    verify(validations).add(notNullReqValidation);
  }

  @Test
  void validate_callCheckMethodOnEachElementOfValidations() {
    // SETUP
    final var sut = mock(ReqValidation.class);
    final var notNullReqValidation = mock(NotNullReqValidation.class);
    final var cipsdLastDayOfMonthReqValidation = mock(CipsdLastDayOfMonthReqValidation.class);
    final var cpedLastDayOfMonthReqValidation = mock(CpedLastDayOfMonthReqValidation.class);
    final var cipsdGreaterThanCpedReqValidation = mock(CipsdGreaterThanCpedReqValidation.class);

    final LinkedList<ReqValidation> validations = new LinkedList<>();
    validations.add(notNullReqValidation);
    validations.add(cipsdLastDayOfMonthReqValidation);
    validations.add(cpedLastDayOfMonthReqValidation);
    validations.add(cipsdGreaterThanCpedReqValidation);
    sut.validations = validations;

    doCallRealMethod().when(sut).validate();

    // ACT
    sut.validate();

    // VERIFY
    validations.forEach(validation -> verify(validation).check());
  }

}