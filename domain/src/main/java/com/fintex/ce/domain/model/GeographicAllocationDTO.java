package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeographicAllocationDTO {

  private String countryId;
  private String countryName;
  private GeographicRegionType region;

}
