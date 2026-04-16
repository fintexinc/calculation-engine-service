package com.fintex.ce.model.domain.calculation.allocation;

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
