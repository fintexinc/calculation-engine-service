package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Schema(description = "Response for rolling-total-returns metric. Contains rolling total returns over moving time windows.")
public class RollingTotalReturnsResDTO extends PeriodResDTO {

  @Schema(description = "Rolling total returns per time window")
  @JsonProperty("rollingTotalReturns")
  private Set<RollingIntervalResDTO> rollingTotalReturns;

}
