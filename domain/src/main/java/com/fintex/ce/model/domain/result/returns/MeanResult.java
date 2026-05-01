package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
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
@Schema(description = "Response for mean metric. Contains arithmetic mean of periodic returns per time interval period.")
public class MeanResult extends PeriodResult {

  @Schema(description = "Arithmetic mean of returns per time interval period")
  private Set<TimeIntervalResult> mean;
}
