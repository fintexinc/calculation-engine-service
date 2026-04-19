package com.fintex.ce.adapter.rest.dto.risk;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for sortino-ratio metric. Contains Sortino ratio (downside risk-adjusted return) per time interval period.")
public class SortinoRatioResDTO extends PeriodResDTO {

  @Schema(description = "Sortino ratio per time interval period")
  private Set<TimeIntervalResDTO> sortinoRatio;

}
