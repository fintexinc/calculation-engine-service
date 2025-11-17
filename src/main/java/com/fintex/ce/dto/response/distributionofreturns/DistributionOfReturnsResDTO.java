package com.fintex.ce.dto.response.distributionofreturns;

import com.fintex.ce.dto.response.core.PeriodResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DistributionOfReturnsResDTO extends PeriodResDTO {

    private DistributionOfReturnsIntervalResDTO monthlyReturns;
    private DistributionOfReturnsIntervalResDTO yearlyReturns;

}
