package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.IntervalResDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RollingIntervalResDTO {

  @JsonProperty("period")
  private String timeIntervalPeriod;
  @JsonProperty("values")
  private Set<IntervalResDTO> rollingReturn;

}