package com.fintex.ce.domain.model.calculation;

import lombok.Getter;

@Getter
public enum ClassificationAllocationType {

  UNCLASSIFIED__UNCLASSIFIED("Unclassified Unclassified"),
  UNCLASSIFIED__CANADA("Unclassified Canada"),
  UNCLASSIFIED__US("Unclassified US"),
  UNCLASSIFIED__INTERNATIONAL_GLOBAL("Unclassified International Global"),
  UNCLASSIFIED__INTERNATIONAL("Unclassified International"),
  UNCLASSIFIED__GLOBAL("Unclassified Global"),
  CASH_AND_CASH_EQUIVALENTS__UNCLASSIFIED("Cash and Cash Equivalents Unclassified"),
  CASH_AND_CASH_EQUIVALENTS__CANADA("Cash and Cash Equivalents Canada"),
  CASH_AND_CASH_EQUIVALENTS__US("Cash and Cash Equivalents US"),
  CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL_GLOBAL("Cash and Cash Equivalents International Global"),
  CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL("Cash and Cash Equivalents International"),
  CASH_AND_CASH_EQUIVALENTS__GLOBAL("Cash and Cash Equivalents Global"),
  FIXED_INCOME__UNCLASSIFIED("Fixed Income Unclassified"),
  FIXED_INCOME__CANADA("Fixed Income Canada"),
  FIXED_INCOME__US("Fixed Income US"),
  FIXED_INCOME__INTERNATIONAL_GLOBAL("Fixed Income International Global"),
  FIXED_INCOME__INTERNATIONAL("Fixed Income International"),
  FIXED_INCOME__GLOBAL("Fixed Income Global"),
  EQUITY__UNCLASSIFIED("Equity Unclassified"),
  EQUITY__CANADA("Equity Canada"),
  EQUITY__US("Equity US"),
  EQUITY__INTERNATIONAL_GLOBAL("Equity International Global"),
  EQUITY__INTERNATIONAL("Equity International"),
  EQUITY__GLOBAL("Equity Global"),
  ALTERNATIVE_INVESTMENTS__UNCLASSIFIED("Alternative Investments Unclassified"),
  ALTERNATIVE_INVESTMENTS__CANADA("Alternative Investments Canada"),
  ALTERNATIVE_INVESTMENTS__US("Alternative Investments US"),
  ALTERNATIVE_INVESTMENTS__INTERNATIONAL_GLOBAL("Alternative Investments International Global"),
  ALTERNATIVE_INVESTMENTS__INTERNATIONAL("Alternative Investments International"),
  ALTERNATIVE_INVESTMENTS__GLOBAL("Alternative Investments Global"),
  MULTICLASS__UNCLASSIFIED("Multiclass Unclassified"),
  MULTICLASS__CANADA("Multiclass Canada"),
  MULTICLASS__US("Multiclass US"),
  MULTICLASS__INTERNATIONAL_GLOBAL("Multiclass International Global"),
  MULTICLASS__INTERNATIONAL("Multiclass International"),
  MULTICLASS__GLOBAL("Multiclass Global"),
  COMMODITIES__UNCLASSIFIED("Commodities Unclassified"),
  COMMODITIES__CANADA("Commodities Canada"),
  COMMODITIES__US("Commodities US"),
  COMMODITIES__INTERNATIONAL_GLOBAL("Commodities International Global"),
  COMMODITIES__INTERNATIONAL("Commodities International"),
  COMMODITIES__GLOBAL("Commodities Global"),
  OTHER__UNCLASSIFIED("Other Unclassified"),
  OTHER__CANADA("Other Canada"),
  OTHER__US("Other US"),
  OTHER__INTERNATIONAL_GLOBAL("Other International Global"),
  OTHER__INTERNATIONAL("Other International"),
  OTHER__GLOBAL("Other Global");

  private final String name;

  ClassificationAllocationType(String name) {
    this.name = name;
  }

  public static ClassificationAllocationType of(final String typeStr) {
    for (ClassificationAllocationType value : values()) {
      if (value.name().equalsIgnoreCase(typeStr)) {
        return value;
      }
    }
    return null;
  }

}
