package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
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
