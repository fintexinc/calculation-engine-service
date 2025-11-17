package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
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
public class EquityCountryExposureResDTO extends WarningDTO {

    private Map<CountryRegionType, BigDecimal> equityCountryExposure;

    public EquityCountryExposureResDTO(Map<CountryRegionType, BigDecimal> equityCountryExposure, List<Warning> warnings) {
        super(warnings);
        this.equityCountryExposure = equityCountryExposure;
    }
}
