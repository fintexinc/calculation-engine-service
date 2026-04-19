package com.fintex.ce.adapter.rest.dto.rolling;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.RollingIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for rolling-correlation metric. Contains rolling correlation over moving time windows.")
public class RollingCorrelationResDTO extends PeriodResDTO {

  @Schema(description = "Rolling correlation per time window")
  private Set<RollingIntervalResDTO> rollingCorrelation;

}
