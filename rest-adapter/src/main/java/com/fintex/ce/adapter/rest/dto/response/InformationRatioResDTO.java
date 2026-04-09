package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.response.core.TimeIntervalResDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
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
@Schema(description = "Response for information-ratio metric. Contains information ratio (active return divided by tracking error) per time interval period.")
public class InformationRatioResDTO extends PeriodResDTO {

  @Schema(description = "Information ratio per time interval period")
  @JsonProperty("informationRatio")
  private Set<TimeIntervalResDTO> timeIntervalResDTOS;

}
