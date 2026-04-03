package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Response for fixed-income-country-exposure metric. Contains fixed income country exposure breakdown.")
public class CountryExposureResDTO extends WarningDTO {
  public CountryExposureResDTO() {
  }
  @Schema(description = "Fixed income country exposure percentages")
  private Map<CountryRegionType, BigDecimal> countryExposure;

  public CountryExposureResDTO(Map<CountryRegionType, BigDecimal> countryExposure, List<Warning> warnings) {
    super(warnings);
    this.countryExposure = countryExposure;
  }
}
