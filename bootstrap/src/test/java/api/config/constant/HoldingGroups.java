package api.config.constant;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.EnumSet;
import java.util.Set;

import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_US;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.MUTUAL_FUND_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_US;

public class HoldingGroups {

  public static final Set<FinancialInstrumentType> FUNDS = EnumSet.of(MUTUAL_FUND_CANADA, ETF_US, ETF_CANADA);
  public static final Set<FinancialInstrumentType> ETFS = EnumSet.of(ETF_US, ETF_CANADA);
  public static final Set<FinancialInstrumentType> STOCKS = EnumSet.of(STOCK_CANADA, STOCK_US);
  public static final Set<FinancialInstrumentType> CASH = EnumSet.of(FinancialInstrumentType.CASH);
  public static final Set<FinancialInstrumentType> MUTUAL_FUND = EnumSet.of(MUTUAL_FUND_CANADA);
  public static final Set<FinancialInstrumentType> GIC = EnumSet.of(FinancialInstrumentType.GIC);
  public static final Set<FinancialInstrumentType> CANADA_POOLED_FUNDS = EnumSet.of(FinancialInstrumentType.POOLED_FUND_CANADA);
  public static final Set<FinancialInstrumentType> CANADA_HEDGE_FUNDS = EnumSet.of(FinancialInstrumentType.HEDGE_FUND_CANADA);
  public static final Set<FinancialInstrumentType> US_MUTUAL_FUNDS = EnumSet.of(FinancialInstrumentType.MUTUAL_FUND_US);
  private HoldingGroups() {
  }

}
