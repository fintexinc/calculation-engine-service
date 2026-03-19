package com.fintex.ce.constant;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.Set;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.util.FinancialInstrumentTypeUtils.getChildren;

@UtilityClass
public class HoldingTypeGroup {

  public static final Set<FinancialInstrumentType> FUNDS = getChildren(FinancialInstrumentType.FUND);

}
