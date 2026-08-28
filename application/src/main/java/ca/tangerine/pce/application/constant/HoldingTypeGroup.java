package ca.tangerine.pce.application.constant;

import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

import static ca.tangerine.wm.commons.domain.utils.FinancialInstrumentTypeUtils.getChildren;
import static ca.tangerine.wm.commons.domain.utils.FinancialInstrumentTypeUtils.getSelfAndChildren;

@UtilityClass
public class HoldingTypeGroup {

  public static final Set<FinancialInstrumentType> FUNDS = getChildren(FinancialInstrumentType.FUND);

  public static final Set<FinancialInstrumentType> MER_BEARING_TYPES = merBearingTypes();

  public static final Set<FinancialInstrumentType> ZERO_MER_TYPES = Collections.unmodifiableSet(EnumSet.of(
      FinancialInstrumentType.STOCK,
      FinancialInstrumentType.CASH,
      FinancialInstrumentType.GIC,
      FinancialInstrumentType.FIXED_INCOME));

  private static Set<FinancialInstrumentType> merBearingTypes() {
    EnumSet<FinancialInstrumentType> union = EnumSet.copyOf(FUNDS);
    union.addAll(getSelfAndChildren(FinancialInstrumentType.ETF));
    return Collections.unmodifiableSet(union);
  }

}
