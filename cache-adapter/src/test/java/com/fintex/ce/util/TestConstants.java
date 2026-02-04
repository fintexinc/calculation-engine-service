package com.fintex.ce.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestConstants {
  public static final LocalDate LOCAL_DATE_NOW = DateTimeUtils.toLastDayOfMonth(LocalDate.now());
  public static final BigDecimal LESS_THAN_YEAR = BigDecimal.valueOf(360);
  public static final BigDecimal GREATER_THAN_YEAR = BigDecimal.valueOf(365);

  private TestConstants() {
  }

}
