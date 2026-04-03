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
@Schema(description = "Response for downside-capture metric. Contains downside capture ratio per time interval period.")
public class DownsideCaptureResDTO extends PeriodResDTO {

  @Schema(description = "Downside capture ratio per time interval period")
  private Set<TimeIntervalResDTO> downsideCapture;

}