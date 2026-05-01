package com.fintex.ce.model.domain.result.correlation;

import java.math.BigDecimal;

public record CorrelationKeyValueResult(String correlationKey, BigDecimal value) {
}
