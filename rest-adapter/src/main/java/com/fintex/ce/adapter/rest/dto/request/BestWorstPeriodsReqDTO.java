package com.fintex.ce.adapter.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BestWorstPeriodsReqDTO extends ReturnReqDTO {

  @JsonProperty("bestWorstTimeIntervalPeriods")
  private Set<Long> bestWorstTimeIntervalPeriods;

}
