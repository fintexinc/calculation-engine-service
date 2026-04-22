package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for management-fee metric. Contains weighted average management fee by parameter type.")
public class ManagementFeeResult extends BaseCalculationResult {

  @Schema(description = "Management fee by parameter type (scaled/absolute)")
  private Map<ParameterType, BigDecimal> managementFee = new HashMap<>();
}
