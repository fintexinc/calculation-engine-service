package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_ALL_GTZ_001;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HoldingValueReqValidatorTest {

  @Test
  void validateHoldings_valueIsNull() {
    // SETUP
    final Holding h = mock(Holding.class);
    when(h.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h.getValue()).thenReturn(null);
    when(h.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(h);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(ERR_ALL_GTZ_001.getMessage(), actual.getMessage());
  }

  @Test
  void validateHoldings_valueIsNegative() {
    // SETUP
    final Holding h = mock(Holding.class);
    when(h.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h.getValue()).thenReturn(BigDecimal.valueOf(-1));
    when(h.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(h);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(ERR_ALL_GTZ_001.getMessage(), actual.getMessage());
  }

  @Test
  void validateHoldings_valueIsZero_checkResult() {
    // SETUP
    final Holding h = mock(Holding.class);
    when(h.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h.getValue()).thenReturn(BigDecimal.valueOf(0));
    when(h.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(h);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT + VERIFY
    assertDoesNotThrow(sut::check);
  }

  @Test
  void validateHoldings_valueIsMoreThanZero_checkResult() {
    // SETUP
    final Holding h = mock(Holding.class);
    when(h.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(h.getValue()).thenReturn(BigDecimal.valueOf(100));
    when(h.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(h);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT + VERIFY
    assertDoesNotThrow(sut::check);
  }

}