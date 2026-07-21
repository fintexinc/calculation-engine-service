package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.contract.BenchmarkHoldingsProvider;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Base command for portfolio-based calculations with holdings and optional benchmark")
public abstract class PortfolioBenchmarkCommand extends PortfolioHoldingsCommand
    implements
      BenchmarkHoldingsProvider {
  @Schema(description = "Benchmark holdings for relative performance calculations")
  private List<PortfolioHolding> benchmarkHoldings;
  @NotNull(message = ErrorCode.Codes.FIELD_NOT_NULL)
  @Schema(description = "Target currency for the calculation", example = "CAD")
  private Currency currency;

  public void setReqCurrencyToCashHolding() {
    final List<CashHolding> cashHoldings = holdings.stream()
        .filter(h -> h.getHoldingType() == FinancialInstrumentType.CASH)
        .map(h -> (CashHolding) h)
        .toList();
    if (cashHoldings.size() == 1 && Objects.isNull(cashHoldings.getFirst().getCurrency())) {
      CashHolding original = cashHoldings.getFirst();
      CashHolding updated = original.toBuilder().currency(currency).build();
      holdings = holdings.stream()
          .map(h -> h == original ? updated : h)
          .toList();
    }
  }
}
