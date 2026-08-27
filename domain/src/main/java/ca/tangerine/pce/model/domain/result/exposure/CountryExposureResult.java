package ca.tangerine.pce.model.domain.result.exposure;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for fixed-income-country-exposure metric. Contains fixed income country exposure breakdown.")
public class CountryExposureResult extends BaseCalculationResult {

  @Schema(description = "Fixed income country exposure percentages")
  private Map<CountryRegionType, BigDecimal> countryExposure;
}
