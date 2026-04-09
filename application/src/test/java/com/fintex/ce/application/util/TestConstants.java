package com.fintex.ce.application.util;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.util.DateTimeUtils;
import com.fintex.sm.model.DataProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestConstants {
  public static final LocalDate LOCAL_DATE_NOW = DateTimeUtils.toLastDayOfMonth(LocalDate.now());
  public static final BigDecimal LESS_THAN_YEAR = BigDecimal.valueOf(360);
  public static final BigDecimal GREATER_THAN_YEAR = BigDecimal.valueOf(365);
  public static final DefaultDataProperties DEFAULT_DATA_PROPERTIES = new DefaultDataProperties(
      List.of(DataProvider.MORNINGSTAR));

}
