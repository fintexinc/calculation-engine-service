package com.fintex.ce.application.util;

import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.util.LinkedHashSet;
import java.util.Set;

public class FinancialInstrumentTypeUtils {

  private FinancialInstrumentTypeUtils() {
  }

  public static Set<FinancialInstrumentType> getChildren(FinancialInstrumentType type) {
    return addChildren(type, new LinkedHashSet<>());
  }

  private static Set<FinancialInstrumentType> addChildren(FinancialInstrumentType type,
      Set<FinancialInstrumentType> allChildren) {
    for (FinancialInstrumentType it : FinancialInstrumentType.values()) {
      if (it.getParent() == type) {
        allChildren.add(it);
        addChildren(it, allChildren);
      }
    }
    return allChildren;
  }

}
