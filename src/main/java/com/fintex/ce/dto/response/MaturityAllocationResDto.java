package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
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
public class MaturityAllocationResDto extends WarningDTO {

    private Map<MaturityAllocationType, BigDecimal> maturityAllocation;

    public MaturityAllocationResDto(Map<MaturityAllocationType, BigDecimal> maturityAllocation, List<Warning> warnings) {
        super(warnings);
        this.maturityAllocation = maturityAllocation;
    }
}
