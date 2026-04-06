package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_DH_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MC_002;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.USD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HoldingReqValidationTest {

  @Test
  void check_duplicatedGicHoldingIsAllowed() {
    final GicHolding gic = GicHolding.builder()
        .value(BigDecimal.ONE)
        .holdingType(FinancialInstrumentType.GIC)
        .investmentDate(LocalDate.now())
        .clientIntRate(BigDecimal.valueOf(100))
        .currency(USD)
        .interestFreq(InterestFreq.MONTHLY)
        .build();

    final List<Holding> holdings = List.of(gic, gic, gic, gic);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_duplicateHoldingsThrowsException() {
    final Holding f = new Holding(BigDecimal.ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("F", FiIdentifierType.FUNDSERV));

    final List<Holding> holdings = List.of(f, f);
    final var sut = new HoldingReqValidation(holdings);

    final ReqValidationException expected = ERR_DH_001.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  void check_multipleCashHoldingsWithoutCurrencyThrowsException() {
    final CashHolding cashWithoutCurrency = CashHolding.builder()
        .holdingType(FinancialInstrumentType.CASH)
        .value(BigDecimal.ONE)
        .build();

    final CashHolding cashWithCurrency = CashHolding.builder()
        .holdingType(FinancialInstrumentType.CASH)
        .value(BigDecimal.ONE)
        .currency(CurrencyType.CAD)
        .build();

    final List<Holding> holdings = List.of(cashWithoutCurrency, cashWithCurrency);
    final var sut = new HoldingReqValidation(holdings);

    final ReqValidationException expected = ERR_RRC_MC_002.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  void check_singleCashHoldingWithoutCurrencyIsAllowed() {
    final CashHolding cashWithoutCurrency = CashHolding.builder()
        .holdingType(FinancialInstrumentType.CASH)
        .value(BigDecimal.ONE)
        .build();

    final List<Holding> holdings = List.of(cashWithoutCurrency);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_gicWithValidInvestmentDatePasses() {
    final GicHolding gic = mock(GicHolding.class);
    when(gic.getInvestmentDate()).thenReturn(LocalDate.now());
    when(gic.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);

    final List<Holding> holdings = List.of(gic);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_gicWithVeryOldInvestmentDateThrowsException() {
    final GicHolding gic = mock(GicHolding.class);
    when(gic.getInvestmentDate()).thenReturn(LocalDate.of(1523, 6, 1));
    when(gic.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);

    final List<Holding> holdings = List.of(gic);
    final var sut = new HoldingReqValidation(holdings);

    assertThrows(ReqValidationException.class, sut::check);
  }

  @Test
  void check_emptyHoldingsListPasses() {
    final var sut = new HoldingReqValidation(List.of());

    assertDoesNotThrow(sut::check);
  }
}
