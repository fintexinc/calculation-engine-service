package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryAllocationDTO {

    private String countryId;
    private String countryName;
    private CountryRegionType region;

}
