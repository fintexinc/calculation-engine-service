package com.fintex.ce.adapter.rest.dto.risk;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Response for alpha metric. Contains Jensen's alpha (excess return from active management) per time interval period.")
public class AlphaResDTO extends PeriodResDTO {

  @Schema(description = "Jensen's alpha per time interval period")
  private Set<TimeIntervalResDTO> alpha;

}
