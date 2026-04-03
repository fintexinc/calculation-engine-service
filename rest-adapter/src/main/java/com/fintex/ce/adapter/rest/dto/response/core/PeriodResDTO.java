package com.fintex.ce.adapter.rest.dto.response.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Base response for period-based calculations containing performance dates.")
public class PeriodResDTO extends WarningDTO {

  @Schema(description = "Performance end date")
  @JsonProperty("performanceEndDate")
  protected LocalDate ped;
  @Schema(description = "Performance start date")
  @JsonProperty("performanceStartDate")
  protected LocalDate psd;
  @Schema(description = "Custom interval performance start date")
  @JsonProperty("customIntervalPerformanceStartDate")
  protected LocalDate customIpsd;

}
