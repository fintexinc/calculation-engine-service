package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CountryExposureResDTO extends WarningDTO {
	public CountryExposureResDTO() {}
    private Map<CountryRegionType, BigDecimal> countryExposure;

    public CountryExposureResDTO(Map<CountryRegionType, BigDecimal> countryExposure, List<Warning> warnings) {
        super(warnings);
        this.countryExposure = countryExposure;
    }
}
