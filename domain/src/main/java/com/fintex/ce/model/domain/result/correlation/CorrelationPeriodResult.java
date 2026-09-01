package com.fintex.ce.model.domain.result.correlation;

import java.math.BigDecimal;
import java.util.Map;

public record CorrelationPeriodResult(String period, String key, Map<String, BigDecimal> correlations) {
}
