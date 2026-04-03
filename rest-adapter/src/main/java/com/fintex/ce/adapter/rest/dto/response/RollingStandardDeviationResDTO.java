package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
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
@Schema(description = "Response for rolling-standard-deviation metric. Contains rolling standard deviation over moving time windows.")
public class RollingStandardDeviationResDTO extends PeriodResDTO {

  @Schema(description = "Rolling standard deviation per time window")
  @JsonProperty("rollingStandardDeviation")
  private Set<RollingIntervalResDTO> rollingStandardDeviation;

}
