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
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@Schema(description = "Response for equity-country-exposure metric. Contains equity country exposure breakdown.")
public class EquityCountryExposureResDTO extends WarningDTO {

  @Schema(description = "Equity exposure percentages by country")
  private Map<CountryRegionType, BigDecimal> equityCountryExposure;

  public EquityCountryExposureResDTO(Map<CountryRegionType, BigDecimal> equityCountryExposure, List<Warning> warnings) {
    super(warnings);
    this.equityCountryExposure = equityCountryExposure;
  }
}
