package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;

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
@Schema(description = "Response for equity-geographic-exposure and fixed-income-geographic-exposure metrics. Contains geographic exposure breakdown by region.")
public class GeographicExposureResult extends BaseCalculationResult {

  @Schema(description = "Geographic exposure percentages by region. Holdings the data source has no record of at "
      + "all, or resolved but did not return a geographic breakdown for, are counted under "
      + "GeographicRegionType.UNKNOWN.")
  private Map<GeographicRegionType, BigDecimal> geographicExposure;
}
