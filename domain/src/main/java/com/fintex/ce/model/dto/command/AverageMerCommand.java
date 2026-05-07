package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Command for fee ratio calculations. Supports metrics: mer, management-fee, fees")
public class AverageMerCommand extends DataProviderCommand implements HoldingsProvider {
  @Schema(description = "Fee aggregation modes to calculate. Wire values map to FeeAggregationMode: "
      + "'scaled' = FUNDS_ONLY (fund holdings only, weights normalised within the fund subset); "
      + "'absolute' = WHOLE_PORTFOLIO (all holdings; non-fund holdings contribute 0% but their market value is in the "
      + "denominator); "
      + "'forceReportFee' = FUNDS_ONLY_STRICT (same set as FUNDS_ONLY, but returns null if any included holding fell "
      + "back to a secondary fee field). "
      + "Defaults to ['scaled', 'absolute'] when omitted.", example = "[\"scaled\", \"absolute\", \"forceReportFee\"]")
  private List<FeeAggregationMode> parameterTypes;
  @Schema(description = "Portfolio holdings")
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  private List<PortfolioHolding> holdings;
}
