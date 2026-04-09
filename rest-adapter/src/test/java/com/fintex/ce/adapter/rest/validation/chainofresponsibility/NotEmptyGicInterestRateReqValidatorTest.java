package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.GicHolding;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_GIC_MC_001;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotEmptyGicInterestRateReqValidatorTest {

  @Test
  void check_ERR_GIC_MC_001ThrownWhenInterestRateIsNull() {
    // SETUP
    final var sut = new NotEmptyGicInterestRateReqValidator(List.of(GicHolding.builder().build()));

    final ReqValidationException expected = ERR_GIC_MC_001.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_nothingThrownIfInterestRateIsEntered() {
    // SETUP
    final GicHolding gic = GicHolding.builder()
        .clientIntRate(BigDecimal.ONE)
        .build();
    final var sut = new NotEmptyGicInterestRateReqValidator(List.of(gic));

    // ACT
    assertDoesNotThrow(() -> sut.check());

    // VERIFY
  }

}
