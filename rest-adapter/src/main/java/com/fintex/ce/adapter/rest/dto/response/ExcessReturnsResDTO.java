package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.response.core.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Schema(description = "Response for excess-returns metric. Contains excess returns relative to benchmark per time interval period.")
public class ExcessReturnsResDTO extends PeriodResDTO {

  @Schema(description = "Excess returns vs benchmark per time interval period")
  private Set<TimeIntervalResDTO> excessReturns;

}
