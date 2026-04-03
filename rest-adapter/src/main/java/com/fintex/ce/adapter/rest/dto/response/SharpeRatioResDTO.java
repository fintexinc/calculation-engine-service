package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.response.core.TimeIntervalResDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for sharpe-ratio metric. Contains Sharpe ratio (risk-adjusted return) per time interval period.")
public class SharpeRatioResDTO extends PeriodResDTO {

  @Schema(description = "Sharpe ratio per time interval period")
  private Set<TimeIntervalResDTO> sharpeRatio;

}
