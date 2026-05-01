package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for fixed-income-credit-quality metric. Contains fixed income credit quality breakdown.")
public class CreditQualityResult extends BaseCalculationResult {

  @Schema(description = "Fixed income credit quality breakdown percentages")
  private Map<FixedIncomeCreditQuality, BigDecimal> creditQuality;
}
