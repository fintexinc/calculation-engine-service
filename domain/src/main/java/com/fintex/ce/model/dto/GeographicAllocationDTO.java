package com.fintex.ce.model.dto;

import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;

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
