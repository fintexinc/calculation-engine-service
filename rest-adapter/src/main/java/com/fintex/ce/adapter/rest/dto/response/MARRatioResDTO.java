package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.response.core.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Response for mar-ratio metric. Contains MAR ratio (return divided by max drawdown) per time interval period.")
public class MARRatioResDTO extends PeriodResDTO {

  @Schema(description = "MAR ratio per time interval period")
  private Set<TimeIntervalResDTO> marRatio;

}