package com.fintex.ce.adapter.rest.dto.response.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatesResDTO extends ErrorDTO {

  @JsonProperty("performanceEndDate")
  protected LocalDate ped;
  @JsonProperty("performanceStartDate")
  protected LocalDate psd;

}
