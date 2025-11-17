package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.FixedIncomeStyleboxType;
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
public class FixedIncomeStyleboxExposureResDto extends WarningDTO {

    private Map<FixedIncomeStyleboxType, BigDecimal> fixedIncomeStyleboxExposure;

    public FixedIncomeStyleboxExposureResDto(Map<FixedIncomeStyleboxType, BigDecimal> fixedIncomeStyleboxExposure, List<Warning> warnings) {
        super(warnings);
        this.fixedIncomeStyleboxExposure = fixedIncomeStyleboxExposure;
    }
}
