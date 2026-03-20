package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import static com.fintex.ce.domain.model.enumeration.Currency.USD;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_DH_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MC_002;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HoldingReqValidationTest {

  @Test
  void check_duplicatedGicHoldingIsAllowed() {
    // GIC holdings are excluded from duplicate check
    final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
    gic.setInvestmentDate(LocalDate.now());
    gic.setClientIntRate(BigDecimal.valueOf(100));
    gic.setCurrency(USD);
    gic.setInterestFreq(InterestFreq.MONTHLY);

    final List<Holding> holdings = List.of(gic, gic, gic, gic);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_duplicateHoldingsThrowsException() {
    final FundSeriesHolding f = new FundSeriesHolding();
    f.setFundServCode("F");
    f.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    f.setValue(BigDecimal.ONE);

    final List<Holding> holdings = List.of(f, f);
    final var sut = new HoldingReqValidation(holdings);

    final ReqValidationException expected = ERR_DH_001.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  void check_multipleCashHoldingsWithoutCurrencyThrowsException() {
    final CashHolding cashWithoutCurrency = new CashHolding();
    cashWithoutCurrency.setType(HoldingType.CASH);
    cashWithoutCurrency.setValue(BigDecimal.ONE);

    final CashHolding cashWithCurrency = new CashHolding();
    cashWithCurrency.setType(HoldingType.CASH);
    cashWithCurrency.setValue(BigDecimal.ONE);
    cashWithCurrency.setCurrency(Currency.CAD);

    final List<Holding> holdings = List.of(cashWithoutCurrency, cashWithCurrency);
    final var sut = new HoldingReqValidation(holdings);

    final ReqValidationException expected = ERR_RRC_MC_002.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  void check_singleCashHoldingWithoutCurrencyIsAllowed() {
    final CashHolding cashWithoutCurrency = new CashHolding();
    cashWithoutCurrency.setType(HoldingType.CASH);
    cashWithoutCurrency.setValue(BigDecimal.ONE);

    final List<Holding> holdings = List.of(cashWithoutCurrency);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_gicWithValidInvestmentDatePasses() {
    final GicHolding gic = mock(GicHolding.class);
    when(gic.getInvestmentDate()).thenReturn(LocalDate.now());
    when(gic.getType()).thenReturn(HoldingType.GIC);

    final List<Holding> holdings = List.of(gic);
    final var sut = new HoldingReqValidation(holdings);

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_gicWithVeryOldInvestmentDateThrowsException() {
    final GicHolding gic = mock(GicHolding.class);
    when(gic.getInvestmentDate()).thenReturn(LocalDate.of(1523, 6, 1));
    when(gic.getType()).thenReturn(HoldingType.GIC);

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
