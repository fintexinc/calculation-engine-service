package com.fintex.ce.model.domain.result.distribution;

import java.math.BigDecimal;

public record DistributionRangeResult(int bin, BigDecimal range, long value) {
}
