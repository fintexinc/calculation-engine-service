package com.fintex.ce.monthlyreturns;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.monthlyreturns.ReturnsCutComponent;
import com.fintex.ce.util.ComparisonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static org.mockito.Mockito.mock;

class MonthlyReturnsCutComponentTest {

  @Test
  void cutReturnsByEndDate_checkResult() {
    // SETUP
    final ReturnsCutComponent sut = new ReturnsCutComponent();
    final Holding h1 = mock(Holding.class);
    final Holding h2 = mock(Holding.class);
    final Holding h3 = mock(Holding.class);

    Map<Holding, TreeMap<LocalDate, BigDecimal>> monthlyReturns = new HashMap<>();

    monthlyReturns.put(h1, getReturnsWithTheSameStartDate(4));
    monthlyReturns.put(h2, getReturnsWithTheSameStartDate(5));
    monthlyReturns.put(h3, getReturnsWithTheSameStartDate(6));

    Map<Holding, TreeMap<LocalDate, BigDecimal>> expected = new HashMap<>();
    expected.put(h1, getReturnsWithTheSameStartDate(4));
    expected.put(h2, getReturnsWithTheSameStartDate(4));
    expected.put(h3, getReturnsWithTheSameStartDate(4));

    // ACT
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> actual = sut.cutReturnsByEndDate(monthlyReturns,
        toLastDayOfMonth(LocalDate.now().plusMonths(4)));

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void cutReturnsByStartDate_checkResult() {
    // SETUP
    final ReturnsCutComponent sut = new ReturnsCutComponent();
    final Holding h1 = mock(Holding.class);
    final Holding h2 = mock(Holding.class);
    final Holding h3 = mock(Holding.class);

    Map<Holding, TreeMap<LocalDate, BigDecimal>> monthlyReturns = new HashMap<>();

    monthlyReturns.put(h1, getReturnsWithTheSameEndDate(4));
    monthlyReturns.put(h2, getReturnsWithTheSameEndDate(5));
    monthlyReturns.put(h3, getReturnsWithTheSameEndDate(6));

    Map<Holding, TreeMap<LocalDate, BigDecimal>> expected = new HashMap<>();
    expected.put(h1, getReturnsWithTheSameEndDate(4));
    expected.put(h2, getReturnsWithTheSameEndDate(4));
    expected.put(h3, getReturnsWithTheSameEndDate(4));

    // ACT
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> actual = sut.cutReturnsByStartDate(monthlyReturns,
        toLastDayOfMonth(LocalDate.now().minusMonths(4)));

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  private TreeMap<LocalDate, BigDecimal> getReturnsWithTheSameStartDate(final int size) {
    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 1; i <= size; i++) {
      returns.put(toLastDayOfMonth(LocalDate.now().plusMonths(i)), BigDecimal.ONE);
    }
    return returns;
  }

  private TreeMap<LocalDate, BigDecimal> getReturnsWithTheSameEndDate(final int size) {
    final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
    for (int i = 1; i <= size; i++) {
      returns.put(toLastDayOfMonth(LocalDate.now().minusMonths(i)), BigDecimal.ONE);
    }
    return returns;
  }
}
