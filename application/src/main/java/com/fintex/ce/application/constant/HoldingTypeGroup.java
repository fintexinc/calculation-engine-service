package com.fintex.ce.application.constant;

import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.application.util.FinancialInstrumentTypeUtils.getChildren;

@UtilityClass
public class HoldingTypeGroup {

  public static final Set<FinancialInstrumentType> FUNDS = getChildren(FinancialInstrumentType.FUND);

  private static final Set<FinancialInstrumentType> FUND_LIKE_TYPES = fundLikeTypes();

  public static final Set<FinancialInstrumentType> CANADIAN_FUND_TYPES = fundLikeByCountry(Country.CANADA);

  public static final Set<FinancialInstrumentType> US_FUND_TYPES = fundLikeByCountry(Country.USA);

  public static final Set<FinancialInstrumentType> MER_BEARING_TYPES;

  static {
    EnumSet<FinancialInstrumentType> merBearing = EnumSet.copyOf(CANADIAN_FUND_TYPES);
    merBearing.addAll(US_FUND_TYPES);
    MER_BEARING_TYPES = Collections.unmodifiableSet(merBearing);
  }

  public static final Set<FinancialInstrumentType> ZERO_MER_TYPES = Collections.unmodifiableSet(EnumSet.of(
      FinancialInstrumentType.STOCK_CANADA,
      FinancialInstrumentType.STOCK_US,
      FinancialInstrumentType.CASH,
      FinancialInstrumentType.GIC,
      FinancialInstrumentType.FIXED_INCOME));

  private static Set<FinancialInstrumentType> fundLikeTypes() {
    EnumSet<FinancialInstrumentType> union = EnumSet.copyOf(FUNDS);
    union.addAll(getChildren(FinancialInstrumentType.ETF));
    return Collections.unmodifiableSet(union);
  }

  private static Set<FinancialInstrumentType> fundLikeByCountry(Country country) {
    return Collections.unmodifiableSet(FUND_LIKE_TYPES.stream()
        .filter(t -> t.getCountry() == country)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(FinancialInstrumentType.class))));
  }

}
