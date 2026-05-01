package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;

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
@Schema(description = "Response for fixed-income-stylebox-exposure metric. Contains fixed income style box exposure breakdown.")
public class FixedIncomeStyleboxExposureResult extends BaseCalculationResult {

  @Schema(description = "Fixed income exposure percentages by style box")
  private Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure;
}
