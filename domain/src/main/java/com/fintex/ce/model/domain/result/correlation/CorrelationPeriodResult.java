package com.fintex.ce.model.domain.result.correlation;

import java.util.List;

public record CorrelationPeriodResult(String period, String key, List<CorrelationKeyValueResult> correlations) {
}
