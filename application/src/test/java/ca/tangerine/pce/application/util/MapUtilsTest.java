package ca.tangerine.pce.application.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static ca.tangerine.pce.application.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.util.ComparisonUtils;

class MapUtilsTest {

  @Test
  void filterWithinRange_isWorking() {
    final DateRange dateRange = new DateRange(LOCAL_DATE_NOW.minusMonths(1), LOCAL_DATE_NOW);
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> mock = Map.of(holding, Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE,
        LOCAL_DATE_NOW.minusMonths(2), ONE,
        LOCAL_DATE_NOW.plusMonths(3), ONE,
        LOCAL_DATE_NOW.plusMonths(1), ONE));

    final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> actual = MapUtils.filterWithinRange(dateRange, mock);

    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(actual, Map.of(holding, Map.of(
        LOCAL_DATE_NOW.minusMonths(1), ONE)));
  }

  @Test
  void overrideDefaultValues_checkResult() {
    final Map<String, BigDecimal> defaultMap = Map.of("1", ZERO, "2", ZERO);
    final Map<String, BigDecimal> userMap = Map.of("1", TEN);

    final Map<String, BigDecimal> actual = MapUtils.overrideDefaultValues(defaultMap, userMap);

    assertEquals(Map.of("1", TEN, "2", ZERO), actual);
  }

}
