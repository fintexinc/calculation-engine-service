package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

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
@Schema(description = "Response for equity-stylebox-exposure metric. Contains equity style box exposure breakdown.")
public class EquityStyleboxExposureResult extends BaseCalculationResult {

  @Schema(description = "Equity exposure percentages by style box")
  private Map<StyleBoxType, BigDecimal> equityStyleboxExposure;
}
