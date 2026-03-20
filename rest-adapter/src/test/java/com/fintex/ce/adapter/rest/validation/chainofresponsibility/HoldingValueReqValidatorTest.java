package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_ALL_GTZ_001;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HoldingValueReqValidatorTest {

  @Test
  void validateHoldings_valueIsNull() {
    // SETUP
    final FundSeriesHolding f = mock(FundSeriesHolding.class);
    when(f.getFundServCode()).thenReturn("F");
    when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(f.getValue()).thenReturn(null);
    when(f.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(f);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(ERR_ALL_GTZ_001.getMessage(), actual.getMessage());
  }

  @Test
  void validateHoldings_valueIsNegative() {
    // SETUP
    final FundSeriesHolding f = mock(FundSeriesHolding.class);
    when(f.getFundServCode()).thenReturn("F");
    when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(f.getValue()).thenReturn(BigDecimal.valueOf(-1));
    when(f.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(f);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(ERR_ALL_GTZ_001.getMessage(), actual.getMessage());
  }

  @Test
  void validateHoldings_valueIsZero_checkResult() {
    // SETUP
    final FundSeriesHolding f = mock(FundSeriesHolding.class);
    when(f.getFundServCode()).thenReturn("F");
    when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(f.getValue()).thenReturn(BigDecimal.valueOf(0));
    when(f.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(f);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT + VERIFY
    assertDoesNotThrow(sut::check);
  }

  @Test
  void validateHoldings_valueIsMoreThanZero_checkResult() {
    // SETUP
    final FundSeriesHolding f = mock(FundSeriesHolding.class);
    when(f.getFundServCode()).thenReturn("F");
    when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
    when(f.getValue()).thenReturn(BigDecimal.valueOf(100));
    when(f.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(f);

    final var sut = new HoldingValueReqValidator(holdings);
    // ACT + VERIFY
    assertDoesNotThrow(sut::check);
  }

}