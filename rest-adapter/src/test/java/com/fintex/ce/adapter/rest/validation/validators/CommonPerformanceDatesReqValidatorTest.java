package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonPerformanceDatesReqValidatorTest {

  private final CommonPerformanceDatesReqValidator validator = new CommonPerformanceDatesReqValidator();

  @Test
  void shouldThrow_whenBenchmarkHasCashWithNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(List.of(cashHolding));
    cmd.setPortfolios(Collections.emptySet());

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_MC_002");
        });
  }

  @Test
  void shouldThrow_whenPortfolioHasCashWithNullCurrency() {
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(null)
        .build();

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(Collections.emptyList());
    cmd.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(List.of(cashHolding))));

    assertThatThrownBy(() -> validator.validate(cmd))
        .isInstanceOf(ReqValidationException.class)
        .satisfies(ex -> {
          ReqValidationException rve = (ReqValidationException) ex;
          assertThat(rve.getCode()).isEqualTo("ERR_RRC_MC_002");
        });
  }

  @Test
  void shouldNotThrow_whenAllHoldingsAreValid() {
    Holding holding = new Holding(
        BigDecimal.TEN,
        FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
    CashHolding cashHolding = CashHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.CASH)
        .currency(CurrencyType.CAD)
        .build();

    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(List.of(holding));
    cmd.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(List.of(cashHolding))));

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenBenchmarkAndPortfoliosAreEmpty() {
    var cmd = new MultiplePortfoliosCommand();
    cmd.setBenchmarkHoldings(Collections.emptyList());
    cmd.setPortfolios(Collections.emptySet());

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }

  @Test
  void shouldNotThrow_whenCommandIsNotMultiplePortfolios() {
    var cmd = new PeriodCommand();
    cmd.setCurrency(CurrencyType.CAD);

    assertThatCode(() -> validator.validate(cmd)).doesNotThrowAnyException();
  }
}
