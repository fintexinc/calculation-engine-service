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
@Schema(description = "Response for tracking-error metric. Contains tracking error (standard deviation of excess returns) per time interval period.")
public class TrackingErrorResDTO extends PeriodResDTO {

  @Schema(description = "Tracking error per time interval period")
  private Set<TimeIntervalResDTO> trackingError;

}