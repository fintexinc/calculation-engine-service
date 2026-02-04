package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RollingStandardDeviationResDTO extends PeriodResDTO {

  @JsonProperty("rollingStandardDeviation")
  private Set<RollingIntervalResDTO> rollingStandardDeviation;

}
