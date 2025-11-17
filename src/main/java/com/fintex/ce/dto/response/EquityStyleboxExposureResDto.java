package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.EquityStyleboxType;
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
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EquityStyleboxExposureResDto extends WarningDTO {

    private Map<EquityStyleboxType, BigDecimal> equityStyleboxExposure;

    public EquityStyleboxExposureResDto(Map<EquityStyleboxType, BigDecimal> equityStyleboxExposure, List<Warning> warnings) {
        super(warnings);
        this.equityStyleboxExposure = equityStyleboxExposure;
    }
}
