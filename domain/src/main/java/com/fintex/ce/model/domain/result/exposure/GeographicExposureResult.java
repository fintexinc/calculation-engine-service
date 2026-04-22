package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

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
@Schema(description = "Response for equity-geographic-exposure and fixed-income-geographic-exposure metrics. Contains geographic exposure breakdown by region.")
public class GeographicExposureResult extends BaseCalculationResult {

  @Schema(description = "Geographic exposure percentages by region")
  private Map<GeographicRegionType, BigDecimal> equityGeographicExposure;
}
