package ca.tangerine.pce.model.domain.result.risk;

import ca.tangerine.pce.model.domain.result.PeriodResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for sharpe-ratio metric. Contains Sharpe ratio (risk-adjusted return) per time interval period.")
public class SharpeRatioResult extends PeriodResult {

  @Schema(description = "Sharpe ratio per time interval period")
  private Map<String, BigDecimal> sharpeRatio;
}
