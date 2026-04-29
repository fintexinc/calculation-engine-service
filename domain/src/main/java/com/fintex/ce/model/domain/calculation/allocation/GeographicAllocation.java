package com.fintex.ce.model.domain.calculation.allocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeographicAllocation {

  private String countryId;
  private String countryName;
  private GeographicRegionType region;

}
