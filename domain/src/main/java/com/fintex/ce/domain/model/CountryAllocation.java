package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.calculation.CountryRegionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryAllocation {

  private String countryId;
  private String countryName;
  private CountryRegionType region;

}
