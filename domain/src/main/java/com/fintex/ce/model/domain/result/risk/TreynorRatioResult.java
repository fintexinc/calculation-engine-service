package com.fintex.ce.model.domain.result.risk;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for treynor-ratio metric. Contains Treynor ratio (excess return per unit of beta) per time interval period.")
public class TreynorRatioResult extends PeriodResult {

  @Schema(description = "Treynor ratio per time interval period")
  private Set<TimeIntervalResult> treynorRatio;
}
