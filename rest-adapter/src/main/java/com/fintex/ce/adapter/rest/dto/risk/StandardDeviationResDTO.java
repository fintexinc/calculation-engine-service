package com.fintex.ce.adapter.rest.dto.risk;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.TimeIntervalResDTO;

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
@Schema(description = "Response for standard-deviation metric. Contains annualized standard deviation per time interval period.")
public class StandardDeviationResDTO extends PeriodResDTO {

  @Schema(description = "Annualized standard deviation per time interval period")
  private Set<TimeIntervalResDTO> standardDeviation;

}
