package ca.tangerine.pce.model.domain.calculation.returns;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import ca.tangerine.wm.commons.error.Notification;

/**
 * Domain interface for returns data used in calculations. Implemented by MonthlyReturns, HistoricalNavPrices, etc.
 */
public interface ReturnsData {

  String getCurrency();

  TreeMap<LocalDate, BigDecimal> getReturns();

  List<Notification> getErrors();

  boolean hasErrors();

  void addError(Notification error);

  /**
   * Check if there are monthly returns specific errors. Default implementation checks if any errors exist.
   */
  default boolean hasMonthlyReturnsErrors() {
    return hasErrors();
  }

  /**
   * Get only monthly returns specific errors. Default implementation returns all errors.
   */
  default List<Notification> getOnlyMonthlyReturnsErrors() {
    return getErrors();
  }

}
