package com.fintex.ce.model.error;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.DataErrorException;
import com.fintex.ce.model.error.exceptions.ReqValidationException;

import java.util.List;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@Getter
public enum ErrorCode {

  WRN_ES_SN_001("The holding is missing values for Sector Name"),
  WRN_EMC_SBV_001("The holding is missing values for Style Box"),

  WRN_UNKNOWN_001("The holding returned an Unknown Type: %s from Data Point %s"),
  WRN_ES_ESA_001("The holding is missing values for Equity Sector Allocation"),
  WRN_ES_ESE_001("The holding is missing values for Equity Stylebox Exposure"),
  WRN_FIS_FISE_001("The holding is missing values for Fixed Income Stylebox Exposure"),
  WRN_CA_CA_001("The holding is missing values for Classification Allocation"),
  WRN_MA_MA_001("The holding is missing values for Maturity Allocation"),
  WRN_FI_DY_001("The holding is missing dividend yield/interest rate value for Income Forecast"),
  WRN_YI_001("The holding is missing dividend yield values for Average Yield calculation"),
  WRN_FI_SC_001("The holding is missing a payout schedule values for Income Forecast"),
  WRN_FI_PF_001("The holding is missing a payment frequency type value for Income Forecast"),
  WRN_FI_MD_001("The holding is missing a maturity date value for Income Forecast"),
  WRN_FI_ID_001("The holding is missing a issue date value for Income Forecast"),
  WRN_RRC_ECE_001("The holding is missing values for Equity Country Exposure"),
  WRN_RRC_EGE_001("The holding is missing values for Equity Geographic Exposure"),
  WRN_EMC_EMC_001("The holding is missing values Equity Market Capitalization"),
  WRN_AA_AA_001("The holding is missing values for Asset Allocation"),
  WRN_FICQ_BCE_001("The holding is missing values for Bond Country Exposure"),
  WRN_CQ_CQ_001("The holding is missing values for Credit Quality"),
  WRN_BS_BS_001("The holding is missing values for Fixed Income Bond Sector"),

  ERR_RRC_MFR_001("Monthly FX rates is missing value for a date %s"),

  ERR_RRC_MMR_001("The holding is missing values for monthly returns"),
  ERR_NAV_PRICES_001("The holding is missing values for historical nav prices"),
  ERR_NAV_PRICES_002("The holding is missing historical nav prices values for month %s"),
  ERR_RRC_MMR_002("The holding is missing monthly return values for date %s"),

  ERR_RRC_CPSD_004("Custom Performance Start Date must be on or before the Custom Performance End Date"),

  ERR_RRC_CPSD_002("Custom Performance Start Date must be on or after the Portfolio Performance Start Date"),
  ERR_RRC_CPSD_003("Custom Performance Start Date must be on or before the Portfolio Performance End Date"),
  ERR_RRC_BMPSD_002("Custom Performance Start Date must be on or after the Benchmark Performance Start Date"),
  ERR_RRC_BMPSD_003("Custom Performance Start Date must be on or before the Benchmark Performance End Date"),

  ERR_RRC_CPED_002("Custom Performance End date must be on or after the Portfolio Performance Start Date"),
  ERR_RRC_CPED_003("Custom Performance End date should be on or before the Portfolio Performance End Date"),
  ERR_RRC_BMPED_002("Custom Performance End date should be on or after the Benchmark Performance Start Date"),
  ERR_RRC_BMPED_003("Custom Performance End date should be on or before the Benchmark Performance End Date"),

  ERR_RRC_TIP_001("Time Interval Period must be >=12"),
  ERR_RRC_TIP_002("Time Interval Period must not include Year to Date"),
  ERR_RRC_TIP_003("Time Interval Period can not be zero or negative value"),
  ERR_RRC_TIP_004("Time Interval Period is not allowed: %s"),
  ERR_RRC_TIP_005("Request must not include Custom Interval Performance Start Date"),
  ERR_RRC_TIP_006("Request must not include Custom Performance End Date"),
  ERR_RRC_TIP_007("Time Interval Period must not include Since Performance Start Date"),
  ERR_RRC_TIP_008("Time Interval Period must not include Since Custom Interval Performance Start Date"),

  ERR_RRC_CPED_001("Custom Performance End Date must be a month-end date"),
  ERR_RRC_CIPSD_001("Custom Interval Performance Start Date must be a month-end date"),
  ERR_RRC_CIPSD_002("Custom Interval Performance Start Date must be on or before the Custom Performance End Date"),
  ERR_RRC_CPSD_001("Custom Performance Start Date must be a month-end date"),

  ERR_RRC_MC_001("The portfolio is missing Currency"),
  ERR_RRC_MC_002("The holding is missing Currency"),

  ERR_FDS_MC_002("The holding is missing Currency. There is no currency value in the FDS response."),

  ERR_FDS_MC_003("Calculation Engine supports only CAD or USD, the currency from fds is %s"),

  WRN_MER_MER_001("The holding is missing Management Expense Ratio"),
  WRN_MER_AMF_001("The holding is missing Actual Management Fee"),
  WRN_MER_NER_001("The holding is missing Net Expense Ratio"),
  WRN_MER_GER_001("The holding is missing Gross Expense Ratio"),
  ERR_MER_MERMF_001("The holding is missing both MER and Management Fee"),
  ERR_MER_NERGER_001("The holding is missing both Net Expense Ratio and Gross Expense Ratio"),
  ERR_MF_MF_001("The holding is missing Management Fee"),
  ERR_SC_SC_001("The holding is missing Sales Charge type"),
  ERR_RRC_RTIP_001("Rolling Period Interval must be greater or equal than 12."),
  ERR_RRC_RTIP_003("Time interval periods for rolling periods must be greater than 0"),

  ERR_BWP_BWPTIP_001("Time interval periods for best/worst periods must be greater than 0"),
  ERR_BWP_BWPTIP_002("Time interval periods for best/worst periods must be less than or equal to 300"),

  WRN_TCH_MUH_001("This holding contains an underlying fund that is missing underlying holdings data"),
  ERR_TCH_MUH_002("This holding is missing underlying holdings data"),
  ERR_TCH_NFM_001("numOfFundsMin must be greater than 0"),
  ERR_TCH_AHT_001("AccumulateHoldingTypes can contain a maximum of 12 holding types"),
  ERR_TCH_NFM_002("Num Of Funds cannot be greater than number of funds in the portfolio"),
  ERR_TCH_GNM_003("Name parameter for GicHolding can not be empty."),

  ERR_RRC_CNOB_001("Custom number of bins must be greater than 5"),
  ERR_RRC_CNOB_002("Custom number of bins must be less than 30"),

  ERR_RRC_MR_001("PortfolioHolding does not contain latest monthly return. Missing timeframe: %s to %s"),
  ERR_RRC_MR_002("PortfolioHolding performance start date is not within common performance date range."),

  ERR_ALL_GTZ_001("Holdings values must be greater than or equal to 0 and must not be null."),

  ERR_MM_001("Valid Share Class required"),
  ERR_MM_002("Valid Category required"),

  WRN_BCC_001("The holding is missing Business country Code"),
  ERR_DH_001("Duplicate holding found in request"),

  ERR_GIC_MC_001("The gic holding is missing interest rate"),
  ERR_GIC_MC_002("The gic holding is missing term"),

  ERR_PI_PI_001("There are no price indices for index."),

  WRN_CHS_001("Company name does not exist for this stock."),

  ERR_SYS_DP_001("Invalid or missing data provider"),

  ERR_VAL_NN_001("%s must not be null"),
  ERR_VAL_NB_001("%s must not be blank"),
  ERR_VAL_NE_001("%s must not be empty");

  public static final List<ErrorCode> FX_RATE_EXCEPTION_CODES = List.of(ERR_RRC_MFR_001);

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }

  public Warning warning(PortfolioHolding h) {
    return new Warning(h.getIdsString(), this.message, this.name());
  }

  public Warning warning(PortfolioHolding h, String param1, String param2) {
    return new Warning(h.getIdsString(), String.format(this.message, param1, param2), this.name());
  }

  public DataErrorException error(PortfolioHolding h) {
    return new DataErrorException(this.message, h.getIdsString(), this);
  }

  public DataErrorException error() {
    return new DataErrorException(this.message, null, this);
  }

  public DataErrorException error(Object param1) {
    return new DataErrorException(String.format(this.message, param1), null, this);
  }

  public DataErrorException errorWithId(String id) {
    return new DataErrorException(String.format(this.message), id, this);
  }

  public DataErrorException error(PortfolioHolding h, String param1, String param2) {
    return new DataErrorException(String.format(this.message, param1, param2), h.getIdsString(), this);
  }

  public DataErrorException error(PortfolioHolding h, Object param1) {
    return new DataErrorException(String.format(this.message, param1), h.getIdsString(), this);
  }

  public ReqValidationException reqValidationError() {
    return new ReqValidationException(this.message, null, this.name());
  }

  public ReqValidationException reqValidationError(Object param1) {
    return new ReqValidationException(String.format(this.message, param1), null, this.name());
  }

  public ReqValidationException reqValidationErrorWithId(String id) {
    return new ReqValidationException(String.format(this.message), id, this.name());
  }

  /**
   * Compile-time string literals of selected {@link ErrorCode} names. Required because Java annotation attributes only
   * accept compile-time constants, so {@code ExceptionCode.X.name()} cannot be used as a Jakarta validation
   * {@code message} value directly.
   */
  @UtilityClass
  public static final class Names {
    public static final String ERR_BWP_BWPTIP_001 = "ERR_BWP_BWPTIP_001";
    public static final String ERR_BWP_BWPTIP_002 = "ERR_BWP_BWPTIP_002";
    public static final String ERR_RRC_CNOB_001 = "ERR_RRC_CNOB_001";
    public static final String ERR_RRC_CNOB_002 = "ERR_RRC_CNOB_002";
    public static final String ERR_RRC_TIP_003 = "ERR_RRC_TIP_003";
    public static final String ERR_VAL_NN_001 = "ERR_VAL_NN_001";
    public static final String ERR_VAL_NB_001 = "ERR_VAL_NB_001";
    public static final String ERR_VAL_NE_001 = "ERR_VAL_NE_001";
  }

}
