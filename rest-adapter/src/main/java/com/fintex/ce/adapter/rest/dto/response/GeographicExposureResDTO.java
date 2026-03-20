package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class GeographicExposureResDTO extends WarningDTO {

  private Map<GeographicRegionType, BigDecimal> equityGeographicExposure;

  public GeographicExposureResDTO(Map<GeographicRegionType, BigDecimal> equityGeographicExposure,
      List<Warning> warnings) {
    super(warnings);
    this.equityGeographicExposure = equityGeographicExposure;
  }
}
