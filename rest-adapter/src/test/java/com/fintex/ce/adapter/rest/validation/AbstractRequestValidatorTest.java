package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AbstractRequestValidatorTest {

  @Test
  void validate_verifyBuild() {
    // SETUP
    final var sut = mock(AbstractRequestValidator.class);

    final var reqDTO = mock(PeriodsReqDTO.class);

    doReturn(ReqValidation.create()).when(sut).build(any());

    doCallRealMethod().when(sut).validate(any());
    // ACT
    sut.validate(reqDTO);

    // VERIFY
    verify(sut).build(reqDTO);
  }

}