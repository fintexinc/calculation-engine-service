package com.fintex.ce.model.domain.result.risk;

import com.fintex.ce.model.domain.result.PeriodResult;

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
@Schema(description = "Response for treynor-ratio metric. Contains Treynor ratio (excess return per unit of beta) per time interval period.")
public class TreynorRatioResult extends PeriodResult {

  @Schema(description = "Treynor ratio per time interval period")
  private Map<String, BigDecimal> treynorRatio;
}
