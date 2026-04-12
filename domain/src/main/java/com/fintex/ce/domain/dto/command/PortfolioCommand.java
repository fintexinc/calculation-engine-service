package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Base command for portfolio-based calculations with holdings and optional benchmark")
public abstract class PortfolioCommand extends CalculationCommand
    implements
      HoldingsProvider,
      BenchmarkHoldingsProvider {

  @Schema(description = "Portfolio holdings to calculate metrics for")
  private List<Holding> holdings;
  @Schema(description = "Benchmark holdings for relative performance calculations")
  private List<Holding> benchmarkHoldings;
  @NotNull(message = ErrorCode.Names.ERR_VAL_NN_001)
  @Schema(description = "Target currency for the calculation", example = "CAD")
  private CurrencyType currency;

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
