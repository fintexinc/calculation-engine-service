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
@Schema(description = "Response for upside-capture metric. Contains upside capture ratio per time interval period.")
public class UpsideCaptureResDTO extends PeriodResDTO {

  @Schema(description = "Upside capture ratio per time interval period")
  private Set<TimeIntervalResDTO> upsideCapture;

}
