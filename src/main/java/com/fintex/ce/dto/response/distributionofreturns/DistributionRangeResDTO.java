package com.fintex.ce.dto.response.distributionofreturns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DistributionRangeResDTO {

    private int bin;
    private BigDecimal range;
    private long value;

}
