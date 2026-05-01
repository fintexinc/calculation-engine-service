package com.fintex.ce.model.domain.result.distribution;

import java.math.BigDecimal;
import java.util.List;

public record DistributionOfReturnsIntervalResult(
    BigDecimal distributionMin,
    BigDecimal distributionMax,
    int distributionBin,
    BigDecimal distributionIncrement,
    List<DistributionRangeResult> distributionRange) {
}
