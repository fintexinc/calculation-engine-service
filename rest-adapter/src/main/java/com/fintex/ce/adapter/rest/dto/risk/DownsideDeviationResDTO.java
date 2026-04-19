package com.fintex.ce.adapter.rest.dto.risk;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Response for downside-deviation metric. Contains downside deviation per time interval period.")
public class DownsideDeviationResDTO extends PeriodResDTO {

  @Schema(description = "Downside deviation per time interval period")
  private Set<TimeIntervalResDTO> downsideDeviation;

}