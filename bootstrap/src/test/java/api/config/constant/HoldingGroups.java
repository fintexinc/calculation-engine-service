package api.config.constant;

import com.fintex.ce.domain.model.enumeration.HoldingType;

import java.util.EnumSet;
import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.HoldingType.*;

public class HoldingGroups {

  public static final Set<HoldingType> FUNDS = EnumSet.of(CANADA_MUTUAL_FUNDS, US_ETF, CANADA_ETF);
  public static final Set<HoldingType> ETFS = EnumSet.of(US_ETF, CANADA_ETF);
  public static final Set<HoldingType> STOCKS = EnumSet.of(CANADA_STOCKS, US_STOCKS);
  public static final Set<HoldingType> CASH = EnumSet.of(HoldingType.CASH);
  public static final Set<HoldingType> MUTUAL_FUND = EnumSet.of(CANADA_MUTUAL_FUNDS);
  public static final Set<HoldingType> GIC = EnumSet.of(HoldingType.GIC);
  public static final Set<HoldingType> CANADA_POOLED_FUNDS = EnumSet.of(HoldingType.CANADA_POOLED_FUNDS);
  public static final Set<HoldingType> CANADA_HEDGE_FUNDS = EnumSet.of(HoldingType.CANADA_HEDGE_FUNDS);
  public static final Set<HoldingType> US_MUTUAL_FUNDS = EnumSet.of(HoldingType.US_MUTUAL_FUNDS);
  private HoldingGroups() {
  }

}
