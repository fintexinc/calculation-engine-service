package ca.tangerine.pce.application.util;

import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskFreeWindowValidatorTest {

  @Test
  void shouldNotThrow_whenEveryWindowDateIsCovered() {
    SortedMap<LocalDate, BigDecimal> window = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> series = new TreeMap<>();
    LocalDate date = LocalDate.of(2025, 1, 31);
    window.put(date, BigDecimal.ONE);
    series.put(date, BigDecimal.valueOf(0.001));

    assertDoesNotThrow(() -> RiskFreeWindowValidator.requireCoverage(window, series));
  }

  @Test
  void shouldThrowMissingTBillRate_whenAWindowDateIsMissing() {
    SortedMap<LocalDate, BigDecimal> window = new TreeMap<>();
    NavigableMap<LocalDate, BigDecimal> series = new TreeMap<>();
    LocalDate covered = LocalDate.of(2025, 1, 31);
    LocalDate missing = LocalDate.of(2025, 2, 28);
    window.put(covered, BigDecimal.ONE);
    window.put(missing, BigDecimal.ONE);
    series.put(covered, BigDecimal.valueOf(0.001));

    CalculationException ex = assertThrows(CalculationException.class,
        () -> RiskFreeWindowValidator.requireCoverage(window, series));
    assertEquals(ErrorCode.MISSING_TBILL_RATE, ex.getErrorCode());
    assertEquals("Missing T-Bill rate for date " + missing, ex.getMessage());
    assertEquals(Map.of("param-1", missing), ex.getMetadata());
  }
}
