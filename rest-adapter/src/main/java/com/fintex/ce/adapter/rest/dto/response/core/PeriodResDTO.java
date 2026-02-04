package com.fintex.ce.adapter.rest.dto.response.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PeriodResDTO extends WarningDTO {

  @JsonProperty("performanceEndDate")
  protected LocalDate ped;
  @JsonProperty("performanceStartDate")
  protected LocalDate psd;
  @JsonProperty("customIntervalPerformanceStartDate")
  protected LocalDate customIpsd;

}
