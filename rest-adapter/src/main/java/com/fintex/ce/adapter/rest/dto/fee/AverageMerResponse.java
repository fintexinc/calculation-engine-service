package com.fintex.ce.adapter.rest.dto.fee;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.domain.enumeration.ParameterType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for mer metric. Contains weighted average management expense ratio (MER) by parameter type.")
public class AverageMerResponse extends WarningDTO {

  @Schema(description = "Management expense ratio by parameter type (scaled/absolute)")
  private Map<ParameterType, BigDecimal> managementExpenseRatio = new HashMap<>();

}
