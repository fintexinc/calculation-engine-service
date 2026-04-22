package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
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
@Schema(description = "Response for fixed-income-bond-sector metric. Contains fixed income bond sector allocation breakdown.")
public class FixedIncomeSectorResult extends BaseCalculationResult {

  @Schema(description = "Fixed income allocation percentages by bond sector")
  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSector;
}
