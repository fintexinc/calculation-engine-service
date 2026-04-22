package com.fintex.ce.model.domain.result.exposure;

import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
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
@Schema(description = "Response for fixed-income-country-exposure metric. Contains fixed income country exposure breakdown.")
public class CountryExposureResult extends BaseCalculationResult {

  @Schema(description = "Fixed income country exposure percentages")
  private Map<CountryRegionType, BigDecimal> countryExposure;
}
