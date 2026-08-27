package ca.tangerine.pce.application.returns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.BasePceException;
import ca.tangerine.wm.commons.error.Notification;

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
