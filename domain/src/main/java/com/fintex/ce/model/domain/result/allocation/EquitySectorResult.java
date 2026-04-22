package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;

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
@Schema(description = "Response for equity-sector metric. Contains equity sector allocation breakdown.")
public class EquitySectorResult extends BaseCalculationResult {

  @Schema(description = "Equity allocation percentages by sector")
  private Map<EquitySectorAllocationType, BigDecimal> equitySector;
}
