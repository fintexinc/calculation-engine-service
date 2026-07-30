package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Command for fee ratio calculations. Supports metrics: mer, management-fee, fees")
public class AverageMerCommand extends PortfolioHoldingsCommand {
  @Schema(description = "Fee aggregation modes to calculate. Wire values map to FeeAggregationMode: "
      + "'scaled' = FUNDS_ONLY (fund holdings only, weights normalised within the fund subset); "
      + "'absolute' = WHOLE_PORTFOLIO (all holdings; non-fund holdings contribute 0% but their market value is in the "
      + "denominator); "
      + "'forceReportFee' = FUNDS_ONLY_STRICT (same set as FUNDS_ONLY, but returns null if any included holding fell "
      + "back to a secondary fee field). "
      + "Defaults to ['scaled', 'absolute'] when omitted.", example = "[\"scaled\", \"absolute\", \"forceReportFee\"]")
  private List<FeeAggregationMode> parameterTypes;

  @Schema(description = "Currency to report money amounts in. Every holding's market value is FX-converted into it "
      + "before weighting or summing, so weights stay comparable across a multi-currency portfolio. Omit to use the "
      + "service's configured reporting currency (CAD).", example = "CAD")
  private Currency targetCurrency;

  @Schema(description = "Periods to project fee amounts over, named or given as a length in months. ONE_MTH reproduces "
      + "the monthly fee and ONE_YR the annual one. Applies only to the metrics that report projected amounts — "
      + "'fees' and 'mer-benchmark-comparison'. 'mer' and 'management-fee' report rates, which have no length, and "
      + "ignore it. Omit to use the service's configured periods.", example = "[\"ONE_MTH\", \"ONE_YR\", \"FIVE_YR\", \"TEN_YR\", \"TWENTY_YR\"]")
  private Set<TimePeriod> projectionPeriods;

  /**
   * An {@code mer} command over {@code holdings} for {@code parameterTypes}, inheriting the data providers and target
   * currency of the command it is derived from. Lets a calculation that runs two sets of holdings through the MER
   * pipeline — {@code mer-benchmark-comparison} and the fee metrics built on it — reuse the caller's settings for both
   * without re-copying fields by hand.
   */
  public static AverageMerCommand of(AverageMerCommand source, List<PortfolioHolding> holdings,
      List<FeeAggregationMode> parameterTypes) {
    AverageMerCommand command = new AverageMerCommand();
    command.setHoldings(holdings);
    command.setParameterTypes(parameterTypes);
    command.setDataProviders(source.getDataProviders());
    command.setTargetCurrency(source.getTargetCurrency());
    command.setProjectionPeriods(source.getProjectionPeriods());
    return command;
  }
}
