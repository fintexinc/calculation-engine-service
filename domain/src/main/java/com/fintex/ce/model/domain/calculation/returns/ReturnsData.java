package com.fintex.ce.model.domain.calculation.returns;

import com.fintex.ce.model.error.ValidationError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

/**
 * Domain interface for returns data used in calculations. Implemented by MonthlyReturns, HistoricalNavPrices, etc.
 */
public interface ReturnsData {

  String getCurrency();

  TreeMap<LocalDate, BigDecimal> getReturns();

  List<ValidationError> getErrors();

  boolean hasErrors();

  void addError(ValidationError error);

  /**
   * Check if there are monthly returns specific errors. Default implementation checks if any errors exist.
   */
  default boolean hasMonthlyReturnsErrors() {
    return hasErrors();
  }

  /**
   * Get only monthly returns specific errors. Default implementation returns all errors.
   */
  default List<ValidationError> getOnlyMonthlyReturnsErrors() {
    return getErrors();
  }

}
