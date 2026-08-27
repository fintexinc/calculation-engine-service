package ca.tangerine.pce.model.domain.result.risk;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.result.MaxDrawdownEntry;
import ca.tangerine.pce.model.domain.result.PeriodResult;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for max-drawdown metric. Contains maximum peak-to-trough drawdown per time interval period.")
public class MaxDrawdownResult extends PeriodResult {

  @Schema(description = "Maximum drawdown entries per time interval period")
  private List<MaxDrawdownEntry> maxDrawdown;
}
