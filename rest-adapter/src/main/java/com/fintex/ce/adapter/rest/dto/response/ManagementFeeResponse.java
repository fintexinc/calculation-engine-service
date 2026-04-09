package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.enumeration.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Response for management-fee metric. Contains weighted average management fee by parameter type.")
public class ManagementFeeResponse extends WarningDTO {

  @Schema(description = "Management fee by parameter type (scaled/absolute)")
  private Map<ParameterType, BigDecimal> managementFee = new HashMap<>();

}
