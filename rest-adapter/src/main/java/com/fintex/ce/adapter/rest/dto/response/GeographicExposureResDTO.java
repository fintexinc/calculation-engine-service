package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
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
@Schema(description = "Response for equity-geographic-exposure and fixed-income-geographic-exposure metrics. Contains geographic exposure breakdown by region.")
public class GeographicExposureResDTO extends WarningDTO {

  @Schema(description = "Geographic exposure percentages by region")
  private Map<GeographicRegionType, BigDecimal> equityGeographicExposure;

  public GeographicExposureResDTO(Map<GeographicRegionType, BigDecimal> equityGeographicExposure,
      List<Warning> warnings) {
    super(warnings);
    this.equityGeographicExposure = equityGeographicExposure;
  }
}
