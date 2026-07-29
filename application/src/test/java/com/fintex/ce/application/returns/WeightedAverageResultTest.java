package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedAverageResultTest {

  @Test
  void shouldDelegateWarningsToSnapshot_whenGetErrorsAsWarnings() {
    BasePceException error = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("hid");
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.<HoldingMonthlyReturns>empty()
        .withErrors(List.of(error));
    TreeMap<LocalDate, BigDecimal> weightedAverage = new TreeMap<>();
    weightedAverage.put(LocalDate.parse("2020-01-31"), BigDecimal.ONE);

    WeightedAverageResult<HoldingMonthlyReturns> result = new WeightedAverageResult<>(weightedAverage, snapshot);

    List<Notification> warnings = result.getErrorsAsWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.getFirst().getCode()).isEqualTo(ErrorCode.Codes.HOLDING_PSD_OUT_OF_RANGE);
    assertThat(warnings.getFirst().getMetadata()).containsEntry("holdingId", "hid");
    assertThat(result.weightedAverage()).containsEntry(LocalDate.parse("2020-01-31"), BigDecimal.ONE);
  }
}
