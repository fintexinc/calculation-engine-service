package ca.tangerine.pce.application.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.experimental.UtilityClass;

import ca.tangerine.pce.application.config.DefaultDataProperties;
import ca.tangerine.pce.util.DateTimeUtils;
import ca.tangerine.wm.commons.domain.DataProvider;

@UtilityClass
public class TestConstants {
  public static final LocalDate LOCAL_DATE_NOW = DateTimeUtils.toLastDayOfMonth(LocalDate.now());
  public static final BigDecimal LESS_THAN_YEAR = BigDecimal.valueOf(360);
  public static final BigDecimal GREATER_THAN_YEAR = BigDecimal.valueOf(365);
  public static final DefaultDataProperties DEFAULT_DATA_PROPERTIES = new DefaultDataProperties(
      List.of(DataProvider.MORNINGSTAR));

}
