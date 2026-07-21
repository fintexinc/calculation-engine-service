package com.fintex.ce.model.error;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.error.HttpStatus;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.model.error.ErrorParams.HOLDING_ID;
import static com.fintex.ce.model.error.ErrorParams.holdingId;
import static com.fintex.ce.model.error.ErrorParams.paramMetadata;
import static com.fintex.ce.model.error.ErrorParams.prepend;

/**
 * Catalog of every error and warning produced by the calculation engine. Each constant carries the code, human-readable
 * message, description, suggested action, HTTP status and severity used when surfacing it through the REST layer.
 * Factory methods on this enum are the canonical way to create {@link CalculationException},
 * {@link ValidationException} or {@link Notification} instances.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // ============================================
  // FDS-xxx — Missing data from external data providers (warnings)
  // ============================================
  MISSING_SECTOR_NAME(
      Codes.MISSING_SECTOR_NAME,
      "The holding %s is missing values for Sector Name",
      "Sector Name is absent in the data provider response for this holding",
      "Populate Sector Name in the source data or verify the data provider mapping",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_STYLE_BOX(
      Codes.MISSING_STYLE_BOX,
      "The holding %s is missing values for Style Box",
      "Style Box is absent in the data provider response for this holding",
      "Populate Style Box values in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  UNKNOWN_TYPE_FROM_DATA_POINT(
      Codes.UNKNOWN_TYPE_FROM_DATA_POINT,
      "The holding %s returned an Unknown Type: %s from Data Point %s",
      "The data provider returned a value that does not map to any known type",
      "Add the unknown value to the corresponding mapping table",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_EQUITY_SECTOR_ALLOCATION(
      Codes.MISSING_EQUITY_SECTOR_ALLOCATION,
      "The holding %s is missing values for Equity Sector Allocation",
      "Equity Sector Allocation is absent in the data provider response",
      "Populate Equity Sector Allocation in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_EQUITY_STYLEBOX_EXPOSURE(
      Codes.MISSING_EQUITY_STYLEBOX_EXPOSURE,
      "The holding %s is missing values for Equity Stylebox Exposure",
      "Equity Stylebox Exposure is absent in the data provider response",
      "Populate Equity Stylebox Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_FIXED_INCOME_STYLEBOX_EXPOSURE(
      Codes.MISSING_FIXED_INCOME_STYLEBOX_EXPOSURE,
      "The holding %s is missing values for Fixed Income Stylebox Exposure",
      "Fixed Income Stylebox Exposure is absent in the data provider response",
      "Populate Fixed Income Stylebox Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_CLASSIFICATION_ALLOCATION(
      Codes.MISSING_CLASSIFICATION_ALLOCATION,
      "The holding %s is missing values for Classification Allocation",
      "Classification Allocation is absent in the data provider response",
      "Populate Classification Allocation in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_MATURITY_ALLOCATION(
      Codes.MISSING_MATURITY_ALLOCATION,
      "The holding %s is missing values for Maturity Allocation",
      "Maturity Allocation is absent in the data provider response",
      "Populate Maturity Allocation in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_INCOME_FORECAST_DIVIDEND_YIELD(
      Codes.MISSING_INCOME_FORECAST_DIVIDEND_YIELD,
      "The holding %s is missing dividend yield/interest rate value for Income Forecast",
      "Dividend yield or interest rate is required for Income Forecast calculations",
      "Populate dividend yield or interest rate for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_YIELD_DIVIDEND_YIELD(
      Codes.MISSING_YIELD_DIVIDEND_YIELD,
      "The holding %s is missing dividend yield values for Average Yield calculation",
      "Dividend yield is required for the Average Yield calculation",
      "Populate dividend yield for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_INCOME_FORECAST_PAYOUT_SCHEDULE(
      Codes.MISSING_INCOME_FORECAST_PAYOUT_SCHEDULE,
      "The holding %s is missing a payout schedule values for Income Forecast",
      "Payout schedule is required for Income Forecast calculations",
      "Populate the payout schedule for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_INCOME_FORECAST_PAYMENT_FREQUENCY(
      Codes.MISSING_INCOME_FORECAST_PAYMENT_FREQUENCY,
      "The holding %s is missing a payment frequency type value for Income Forecast",
      "Payment frequency is required for Income Forecast calculations",
      "Populate the payment frequency type for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_INCOME_FORECAST_MATURITY_DATE(
      Codes.MISSING_INCOME_FORECAST_MATURITY_DATE,
      "The holding %s is missing a maturity date value for Income Forecast",
      "Maturity date is required for Income Forecast calculations",
      "Populate the maturity date for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_INCOME_FORECAST_ISSUE_DATE(
      Codes.MISSING_INCOME_FORECAST_ISSUE_DATE,
      "The holding %s is missing a issue date value for Income Forecast",
      "Issue date is required for Income Forecast calculations",
      "Populate the issue date for this holding",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_EQUITY_COUNTRY_EXPOSURE(
      Codes.MISSING_EQUITY_COUNTRY_EXPOSURE,
      "The holding %s is missing values for Equity Country Exposure",
      "Equity Country Exposure is absent in the data provider response",
      "Populate Equity Country Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_EQUITY_GEOGRAPHIC_EXPOSURE(
      Codes.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE,
      "The holding %s is missing values for Equity Geographic Exposure",
      "Equity Geographic Exposure is absent in the data provider response",
      "Populate Equity Geographic Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_EQUITY_MARKET_CAPITALIZATION(
      Codes.MISSING_EQUITY_MARKET_CAPITALIZATION,
      "The holding %s is missing values Equity Market Capitalization",
      "Equity Market Capitalization is absent in the data provider response",
      "Populate Equity Market Capitalization in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_ASSET_ALLOCATION(
      Codes.MISSING_ASSET_ALLOCATION,
      "The holding %s is missing values for Asset Allocation",
      "Asset Allocation is absent in the data provider response",
      "Populate Asset Allocation in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_BOND_COUNTRY_EXPOSURE(
      Codes.MISSING_BOND_COUNTRY_EXPOSURE,
      "The holding %s is missing values for Bond Country Exposure",
      "Bond Country Exposure is absent in the data provider response",
      "Populate Bond Country Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_CREDIT_QUALITY(
      Codes.MISSING_CREDIT_QUALITY,
      "The holding %s is missing values for Credit Quality",
      "Credit Quality is absent in the data provider response",
      "Populate Credit Quality in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_FIXED_INCOME_BOND_SECTOR(
      Codes.MISSING_FIXED_INCOME_BOND_SECTOR,
      "The holding %s is missing values for Fixed Income Bond Sector",
      "Fixed Income Bond Sector is absent in the data provider response",
      "Populate Fixed Income Bond Sector in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_MANAGEMENT_EXPENSE_RATIO(
      Codes.MISSING_MANAGEMENT_EXPENSE_RATIO,
      "The holding %s is missing Management Expense Ratio",
      "Management Expense Ratio (MER) is absent in the data provider response",
      "Populate MER in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_ACTUAL_MANAGEMENT_FEE(
      Codes.MISSING_ACTUAL_MANAGEMENT_FEE,
      "The holding %s is missing Actual Management Fee",
      "Actual Management Fee is absent in the data provider response",
      "Populate Actual Management Fee in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_NET_EXPENSE_RATIO(
      Codes.MISSING_NET_EXPENSE_RATIO,
      "The holding %s is missing Net Expense Ratio",
      "Net Expense Ratio is absent in the data provider response",
      "Populate Net Expense Ratio in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_GROSS_EXPENSE_RATIO(
      Codes.MISSING_GROSS_EXPENSE_RATIO,
      "The holding %s is missing Gross Expense Ratio",
      "Gross Expense Ratio is absent in the data provider response",
      "Populate Gross Expense Ratio in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_BUSINESS_COUNTRY_CODE(
      Codes.MISSING_BUSINESS_COUNTRY_CODE,
      "The holding %s is missing Business country Code",
      "Business country code is absent in the data provider response",
      "Populate the Business country code in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_COMPANY_NAME(
      Codes.MISSING_COMPANY_NAME,
      "Company name does not exist for stock %s.",
      "Company name is absent in the data provider response for this stock",
      "Populate the company name for this stock",
      HttpStatus.OK,
      Severity.WARNING),

  UNDERLYING_FUND_MISSING_UNDERLYING_HOLDINGS(
      Codes.UNDERLYING_FUND_MISSING_UNDERLYING_HOLDINGS,
      "Holding %s contains an underlying fund that is missing underlying holdings data",
      "An underlying fund has no underlying holdings data in the data provider response",
      "Populate underlying holdings data for the underlying fund",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_HOLDING_IDENTIFIERS(
      Codes.MISSING_HOLDING_IDENTIFIERS,
      "%s portfolio securities are missing underlying holding identifiers of the configured comparison type",
      "Some portfolio securities had no underlying holding identifiers of the configured comparison type returned by the data provider",
      "Populate underlying holding identifiers for the affected securities in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_UNDERLYING_HOLDING_ID_VALUE(
      Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE,
      "%s underlying holdings have a null identifier value for the configured comparison type",
      "The data provider returned underlying holdings whose identifier value is null for the configured comparison type",
      "Populate identifier values for the affected underlying holdings in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE(
      Codes.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE,
      "The holding %s is missing values for Fixed Income Geographic Exposure",
      "Fixed Income Geographic Exposure is absent in the data provider response",
      "Populate Fixed Income Geographic Exposure in the source data",
      HttpStatus.OK,
      Severity.WARNING),

  // ============================================
  // FX-xxx — FX rate errors
  // ============================================
  FX_RATES_UNAVAILABLE(
      Codes.FX_RATES_UNAVAILABLE,
      "FX rates unavailable for holding %s: %s -> %s",
      "FX rates could not be obtained for some month-ends in the requested range; the holding's contribution is computed from the available months only",
      "Verify Bank of Canada availability and that the currency pair is configured; ensure rates exist for the requested date range",
      HttpStatus.BAD_REQUEST,
      Severity.WARNING),

  // ============================================
  // RET-xxx — Returns / NAV data errors
  // ============================================
  MISSING_MONTHLY_RETURNS(
      Codes.MISSING_MONTHLY_RETURNS,
      "The holding is missing values for monthly returns",
      "Monthly returns data is required for this calculation",
      "Populate monthly returns for this holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_HISTORICAL_NAV_PRICES(
      Codes.MISSING_HISTORICAL_NAV_PRICES,
      "The holding is missing values for historical nav prices",
      "Historical NAV prices are required for this calculation",
      "Populate historical NAV prices for this holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_HISTORICAL_NAV_PRICES_FOR_MONTH(
      Codes.MISSING_HISTORICAL_NAV_PRICES_FOR_MONTH,
      "The holding is missing historical nav prices values for month %s",
      "Historical NAV price is missing for the specified month",
      "Populate the NAV price for the missing month",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_MONTHLY_RETURN_FOR_DATE(
      Codes.MISSING_MONTHLY_RETURN_FOR_DATE,
      "The holding is missing monthly return values for date %s",
      "Monthly return is missing for the specified date",
      "Populate the monthly return for the missing date",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_MISSING_LATEST_MONTHLY_RETURN(
      Codes.HOLDING_MISSING_LATEST_MONTHLY_RETURN,
      "Holding does not contain latest monthly return. Missing timeframe: %s to %s",
      "The holding does not have a monthly return in the required timeframe",
      "Populate monthly returns for the missing timeframe",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD(
      Codes.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD,
      "Insufficient monthly returns to calculate period %s months (only %s available)",
      "The requested period exceeds the number of monthly returns available, so the metric cannot be computed",
      "Request a smaller period or extend the available monthly returns history",
      HttpStatus.OK,
      Severity.WARNING),

  CIPSD_OUTSIDE_DATA_RANGE(
      Codes.CIPSD_OUTSIDE_DATA_RANGE,
      "CIPSD %s is outside the available monthly returns range [%s, %s]",
      "The custom interval performance start date falls outside the available monthly returns window, so no months are available for the requested interval and the since-custom-interval period cannot be computed",
      "Pick a CIPSD on or after the earliest month-end and on or before the latest month-end of the available returns, or extend the returns history",
      HttpStatus.OK,
      Severity.WARNING),

  HOLDING_PSD_OUT_OF_RANGE(
      Codes.HOLDING_PSD_OUT_OF_RANGE,
      "Holding performance start date is not within common performance date range.",
      "The holding performance start date falls outside the common performance range of all holdings",
      "Adjust the portfolio so that performance start dates overlap",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  NAV_PARAM_MISSING(
      Codes.NAV_PARAM_MISSING,
      "Missing parameter while resolving available NAV date. Name: %s. Value: %s",
      "A server-side invariant was violated — a required parameter was null when computing the earliest or latest available NAV date",
      "Contact support; this indicates a bug in the calculation pipeline rather than invalid input",
      HttpStatus.INTERNAL_SERVER_ERROR,
      Severity.ERROR),

  NO_COMPLETE_CALENDAR_YEAR(
      Codes.NO_COMPLETE_CALENDAR_YEAR,
      "No complete calendar year (Jan-Dec) found in monthly returns range [%s, %s]; annual returns cannot be computed",
      "Annual returns require at least one calendar year with all 12 monthly returns present, but the available data window does not cover any full year",
      "Extend the holding's monthly returns history so that at least one calendar year (Jan-Dec) is fully covered",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  INCOMPLETE_YEAR_SKIPPED(
      Codes.INCOMPLETE_YEAR_SKIPPED,
      "Annual return for year %s cannot be computed: only %s of 12 monthly returns available",
      "The calendar year cannot be computed because some monthly returns within January–December are missing",
      "Populate the missing monthly returns for the affected year",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  DEGENERATE_GROWTH_DATA(
      Codes.DEGENERATE_GROWTH_DATA,
      "Growth-of-10K series collapsed to zero for period %s; max drawdown cannot be computed",
      "All peak values in the growth-of-10K window are zero, typically caused by a −100% monthly return, making the drawdown ratio undefined",
      "Review monthly returns for extreme negative values; if a −100% return is expected, the result will remain null",
      HttpStatus.OK,
      Severity.WARNING),

  MISSING_PORTFOLIO_RETURN_FOR_DATE(
      Codes.MISSING_PORTFOLIO_RETURN_FOR_DATE,
      "Portfolio is missing monthly return values for date %s",
      "Portfolio monthly return coverage must exactly match every month in the requested calculation interval",
      "Populate the missing portfolio monthly return or request a period covered by the portfolio data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_BENCHMARK_RETURN_FOR_DATE(
      Codes.MISSING_BENCHMARK_RETURN_FOR_DATE,
      "Benchmark is missing monthly return values for date %s",
      "Benchmark monthly return coverage must exactly match every month in the requested calculation interval",
      "Populate the missing benchmark monthly return or request a period covered by the benchmark data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  PERIOD_RESULT_NOT_AVAILABLE(
      Codes.PERIOD_RESULT_NOT_AVAILABLE,
      "Period %s for metric %s could not be calculated from the available input data",
      "The requested period had enough monthly observations, but the calculation result is undefined or unavailable for the metric-specific input shape",
      "Review the input data for zero denominators, zero variance, empty qualifying windows, or missing aligned dates",
      HttpStatus.OK,
      Severity.WARNING),

  // ============================================
  // SEC-xxx — Security catalog (existence) errors
  // ============================================
  NO_SECURITY_DATA_FOR_HOLDING(
      Codes.NO_SECURITY_DATA_FOR_HOLDING,
      "No data returned for holding %s",
      "The data source did not include this holding in its response — the security is unknown or the configured "
          + "data provider has no record for it on the requested attribute",
      "Verify the security identifier is valid, switch to a data provider that covers it, or remove the holding "
          + "from the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  SECURITY_NOT_FOUND_FOR_METRIC(
      Codes.SECURITY_NOT_FOUND_FOR_METRIC,
      "Security information not found by the data source for %2$s",
      "The data source has no record of this security at all (not merely missing a specific attribute). "
          + "Depending on the metric, its value is either allocated to Unclassified so portfolio totals still "
          + "sum to 100%, or counted as its own unresolved holding",
      "Verify the security identifier is valid or switch to a data provider that covers it",
      HttpStatus.OK,
      Severity.WARNING),

  // ============================================
  // TBL-xxx — T-Bill (risk-free rate) errors
  // ============================================
  MISSING_TBILL_RATE(
      Codes.MISSING_TBILL_RATE,
      "Missing T-Bill rate for date %s",
      "T-Bill rate is required for every month in the calculation interval but is missing for the specified date",
      "Ensure T-Bill rates are available for the requested currency and date range",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY(
      Codes.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY,
      "T-Bill rates are not available for currency %s",
      "The metric requires a T-Bill (risk-free rate) series but Security Master provides no rates for the requested currency",
      "Use a supported currency (CAD or USD) or ensure T-Bill rates are available for the requested currency",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // PFD-xxx — Custom Performance Date validation errors
  // ============================================
  CPSD_AFTER_CPED(
      Codes.CPSD_AFTER_CPED,
      "Custom Performance Start Date must be on or before the Custom Performance End Date",
      "CPSD is after CPED",
      "Ensure CPSD is on or before CPED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPSD_BEFORE_PORTFOLIO_PSD(
      Codes.CPSD_BEFORE_PORTFOLIO_PSD,
      "Custom Performance Start Date must be on or after the Portfolio Performance Start Date",
      "CPSD is earlier than the portfolio's earliest available performance date",
      "Set CPSD to a date on or after the Portfolio PSD",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPSD_AFTER_PORTFOLIO_PED(
      Codes.CPSD_AFTER_PORTFOLIO_PED,
      "Custom Performance Start Date must be on or before the Portfolio Performance End Date",
      "CPSD is later than the portfolio's latest available performance date",
      "Set CPSD to a date on or before the Portfolio PED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPSD_BEFORE_BENCHMARK_PSD(
      Codes.CPSD_BEFORE_BENCHMARK_PSD,
      "Custom Performance Start Date must be on or after the Benchmark Performance Start Date",
      "CPSD is earlier than the benchmark's earliest available performance date",
      "Set CPSD to a date on or after the Benchmark PSD",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPSD_AFTER_BENCHMARK_PED(
      Codes.CPSD_AFTER_BENCHMARK_PED,
      "Custom Performance Start Date must be on or before the Benchmark Performance End Date",
      "CPSD is later than the benchmark's latest available performance date",
      "Set CPSD to a date on or before the Benchmark PED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPED_BEFORE_PORTFOLIO_PSD(
      Codes.CPED_BEFORE_PORTFOLIO_PSD,
      "Custom Performance End date must be on or after the Portfolio Performance Start Date",
      "CPED is earlier than the portfolio's earliest available performance date",
      "Set CPED to a date on or after the Portfolio PSD",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPED_AFTER_PORTFOLIO_PED(
      Codes.CPED_AFTER_PORTFOLIO_PED,
      "Custom Performance End date should be on or before the Portfolio Performance End Date",
      "CPED is later than the portfolio's latest available performance date",
      "Set CPED to a date on or before the Portfolio PED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPED_BEFORE_BENCHMARK_PSD(
      Codes.CPED_BEFORE_BENCHMARK_PSD,
      "Custom Performance End date should be on or after the Benchmark Performance Start Date",
      "CPED is earlier than the benchmark's earliest available performance date",
      "Set CPED to a date on or after the Benchmark PSD",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPED_AFTER_BENCHMARK_PED(
      Codes.CPED_AFTER_BENCHMARK_PED,
      "Custom Performance End date should be on or before the Benchmark Performance End Date",
      "CPED is later than the benchmark's latest available performance date",
      "Set CPED to a date on or before the Benchmark PED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPED_NOT_MONTH_END(
      Codes.CPED_NOT_MONTH_END,
      "Custom Performance End Date must be a month-end date",
      "CPED must coincide with the last calendar day of a month",
      "Set CPED to a month-end date",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CIPSD_NOT_MONTH_END(
      Codes.CIPSD_NOT_MONTH_END,
      "Custom Interval Performance Start Date must be a month-end date",
      "CIPSD must coincide with the last calendar day of a month",
      "Set CIPSD to a month-end date",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CIPSD_AFTER_CPED(
      Codes.CIPSD_AFTER_CPED,
      "Custom Interval Performance Start Date must be on or before the Custom Performance End Date",
      "CIPSD is after CPED",
      "Set CIPSD to a date on or before CPED",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CPSD_NOT_MONTH_END(
      Codes.CPSD_NOT_MONTH_END,
      "Custom Performance Start Date must be a month-end date",
      "CPSD must coincide with the last calendar day of a month",
      "Set CPSD to a month-end date",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // TIP-xxx — Time Interval Period errors
  // ============================================
  TIME_INTERVAL_PERIOD_LESS_THAN_12(
      Codes.TIME_INTERVAL_PERIOD_LESS_THAN_12,
      "Time Interval Period must be >=12",
      "A numeric time interval period below 12 was supplied",
      "Use time interval periods of 12 or more",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE(
      Codes.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE,
      "Time Interval Period must not include Year to Date",
      "YEAR_TO_DATE is not supported for this metric",
      "Remove YEAR_TO_DATE from the requested periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TIME_INTERVAL_PERIOD_NOT_POSITIVE(
      Codes.TIME_INTERVAL_PERIOD_NOT_POSITIVE,
      "Time Interval Period can not be zero or negative value",
      "A non-positive time interval period was supplied",
      "Use positive integer values for time interval periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TIME_INTERVAL_PERIOD_NOT_ALLOWED(
      Codes.TIME_INTERVAL_PERIOD_NOT_ALLOWED,
      "Time Interval Period is not allowed: %s",
      "The supplied time interval period is not accepted by this metric",
      "Use one of the accepted time interval periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD(
      Codes.REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD,
      "Request must not include Custom Interval Performance Start Date",
      "CIPSD is not allowed with the current time interval period selection",
      "Remove Custom Interval Performance Start Date from the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  REQUEST_CONTAINS_CUSTOM_PED(
      Codes.REQUEST_CONTAINS_CUSTOM_PED,
      "Request must not include Custom Performance End Date",
      "CPED is not allowed with the current time interval period selection",
      "Remove Custom Performance End Date from the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TIME_INTERVAL_PERIOD_CONTAINS_SINCE_PSD(
      Codes.TIME_INTERVAL_PERIOD_CONTAINS_SINCE_PSD,
      "Time Interval Period must not include Since Performance Start Date",
      "SINCE_PERFORMANCE_START_DATE is not supported for this metric",
      "Remove SINCE_PERFORMANCE_START_DATE from the requested periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  TIME_INTERVAL_PERIOD_CONTAINS_SINCE_CIPSD(
      Codes.TIME_INTERVAL_PERIOD_CONTAINS_SINCE_CIPSD,
      "Time Interval Period must not include Since Custom Interval Performance Start Date",
      "SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE is not supported for this metric",
      "Remove SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE from the requested periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  ROLLING_INTERVAL_LESS_THAN_12(
      Codes.ROLLING_INTERVAL_LESS_THAN_12,
      "Rolling Period Interval must be greater or equal than 12.",
      "Rolling period interval below 12 was supplied",
      "Use rolling period intervals of 12 or more",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  ROLLING_TIME_INTERVAL_NOT_POSITIVE(
      Codes.ROLLING_TIME_INTERVAL_NOT_POSITIVE,
      "Time interval periods for rolling periods must be greater than 0",
      "A non-positive rolling time interval period was supplied",
      "Use positive integer values for rolling time interval periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // MET-xxx — Calculation metric request errors
  // ============================================
  UNSUPPORTED_METRIC(
      Codes.UNSUPPORTED_METRIC,
      "Calculation metric is not supported: %s",
      "The requested metric is not registered with any calculation service",
      "Use one of the supported metrics listed in the API documentation",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  METRIC_MISMATCH(
      Codes.METRIC_MISMATCH,
      "Metric mismatch: path parameter is '%s' but request body contains '%s'",
      "The metric value supplied in the request body does not match the path variable",
      "Remove the metric field from the body or make it match the path metric",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  DUPLICATE_METRIC(
      Codes.DUPLICATE_METRIC,
      "Duplicate calculation metric in composite request: %s",
      "Each metric may appear at most once in a composite calculation request",
      "Remove the duplicated command or merge the duplicates into a single command",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  METRIC_REQUIRED(
      Codes.METRIC_REQUIRED,
      "Calculation command is missing the metric discriminator",
      "Every command in a composite calculation request must carry a supported 'metric' value",
      "Add a supported metric field to each command in the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // CUR-xxx — Currency errors
  // ============================================
  PORTFOLIO_MISSING_CURRENCY(
      Codes.PORTFOLIO_MISSING_CURRENCY,
      "The portfolio is missing Currency",
      "Portfolio currency was not supplied in the request",
      "Specify the portfolio currency in the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_MISSING_CURRENCY(
      Codes.HOLDING_MISSING_CURRENCY,
      "The holding is missing Currency",
      "Holding currency was not supplied in the request",
      "Specify the currency for this holding in the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_MISSING_CURRENCY_FROM_FDS(
      Codes.HOLDING_MISSING_CURRENCY_FROM_FDS,
      "The holding is missing Currency. There is no currency value in the FDS response.",
      "Currency is absent from the data provider response for this holding",
      "Populate currency in the source data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  UNSUPPORTED_CURRENCY_FROM_FDS(
      Codes.UNSUPPORTED_CURRENCY_FROM_FDS,
      "Calculation Engine supports only CAD or USD, the currency from fds is %s",
      "The data provider returned a currency that is not supported",
      "Ensure data provider returns CAD or USD for the holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // MER-xxx — MER / Fee errors
  // ============================================
  MISSING_MER_AND_MANAGEMENT_FEE(
      Codes.MISSING_MER_AND_MANAGEMENT_FEE,
      "The holding is missing both MER and Management Fee",
      "Neither MER nor Management Fee is available for this holding",
      "Populate MER or Management Fee in the source data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_FUND_FEE_DATA(
      Codes.MISSING_FUND_FEE_DATA,
      "The holding %s has no fee data; one of MER, Net Expense Ratio, Gross Expense Ratio, "
          + "or Management Fee is required",
      "All four fee fields (MER, NER, GER, Management Fee) are null for this fund holding",
      "Populate at least one of MER, NER, GER, or Management Fee for this security",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_NER_AND_GER(
      Codes.MISSING_NER_AND_GER,
      "The holding is missing both Net Expense Ratio and Gross Expense Ratio",
      "Neither Net Expense Ratio nor Gross Expense Ratio is available for this holding",
      "Populate Net Expense Ratio or Gross Expense Ratio in the source data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_MANAGEMENT_FEE(
      Codes.MISSING_MANAGEMENT_FEE,
      "The holding is missing Management Fee",
      "Management Fee is required for this calculation and is not available",
      "Populate Management Fee in the source data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  MISSING_SALES_CHARGE_TYPE(
      Codes.MISSING_SALES_CHARGE_TYPE,
      "The holding is missing Sales Charge type",
      "Sales Charge type is required for this calculation and is not available",
      "Populate Sales Charge type in the source data",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // BWP-xxx — Best/Worst Periods errors
  // ============================================
  BEST_WORST_TIME_INTERVAL_NOT_POSITIVE(
      Codes.BEST_WORST_TIME_INTERVAL_NOT_POSITIVE,
      "Time interval periods for best/worst periods must be greater than 0",
      "A non-positive time interval period was supplied for Best/Worst Periods",
      "Use positive integer values for best/worst time interval periods",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  BEST_WORST_TIME_INTERVAL_TOO_LARGE(
      Codes.BEST_WORST_TIME_INTERVAL_TOO_LARGE,
      "Time interval periods for best/worst periods must be less than or equal to 300",
      "A time interval period above 300 was supplied for Best/Worst Periods",
      "Use time interval periods less than or equal to 300",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // TCH-xxx — Top Common Holdings errors
  // ============================================
  HOLDING_MISSING_UNDERLYING_HOLDINGS(
      Codes.HOLDING_MISSING_UNDERLYING_HOLDINGS,
      "This holding is missing underlying holdings data",
      "Underlying holdings data is required for Top Common Holdings calculation",
      "Populate underlying holdings data for this holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_MISSING_WEIGHTING_FROM_FDS(
      Codes.HOLDING_MISSING_WEIGHTING_FROM_FDS,
      "Underlying holding %s is missing weighting in the data provider response",
      "Weighting is required to decompose a fund into its underlying holdings; defaulting it would silently drop "
          + "the affected branch from the result",
      "Populate the weighting field in the source data for this underlying holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  ACCUMULATE_HOLDING_TYPES_EXCEED_MAX(
      Codes.ACCUMULATE_HOLDING_TYPES_EXCEED_MAX,
      "AccumulateHoldingTypes can contain a maximum of 12 holding types",
      "Too many holding types were requested for accumulation",
      "Reduce the number of accumulate holding types to 12 or fewer",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  GIC_HOLDING_NAME_EMPTY(
      Codes.GIC_HOLDING_NAME_EMPTY,
      "Name parameter for GicHolding can not be empty.",
      "GIC holding name is required",
      "Populate the name parameter for GIC holdings",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // DIS-xxx — Distribution of Returns errors
  // ============================================
  CUSTOM_NUMBER_OF_BINS_LESS_THAN_MIN(
      Codes.CUSTOM_NUMBER_OF_BINS_LESS_THAN_MIN,
      "Custom number of bins must be greater than 5",
      "Custom number of bins must be at least 5",
      "Use a customNumberOfBins value of at least 5",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  CUSTOM_NUMBER_OF_BINS_GREATER_THAN_MAX(
      Codes.CUSTOM_NUMBER_OF_BINS_GREATER_THAN_MAX,
      "Custom number of bins must be less than 30",
      "Custom number of bins must not exceed 30",
      "Use a customNumberOfBins value less than or equal to 30",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // HLD-xxx — Holdings validation errors
  // ============================================
  HOLDING_VALUE_NEGATIVE_OR_NULL(
      Codes.HOLDING_VALUE_NEGATIVE_OR_NULL,
      "Holdings values must be greater than or equal to 0 and must not be null.",
      "A holding was supplied with a negative or null value",
      "Set the holding value to a non-negative number",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_VALUES_SUM_NOT_POSITIVE(
      Codes.HOLDING_VALUES_SUM_NOT_POSITIVE,
      "Sum of holding values must be greater than 0.",
      "All supplied holdings have zero values, so portfolio weights cannot be calculated",
      "Provide at least one holding with a positive value",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  DUPLICATE_HOLDING(
      Codes.DUPLICATE_HOLDING,
      "Duplicate holding with id %s found in request",
      "The request contains two or more identical holdings",
      "Remove duplicate holdings from the request",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  DUPLICATE_CASH_HOLDING(
      Codes.DUPLICATE_CASH_HOLDING,
      "Duplicate cash holding with currency %s found in request",
      "The request contains two or more cash holdings with the same currency",
      "Provide at most one cash holding per currency",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  DUPLICATE_GIC_HOLDING(
      Codes.DUPLICATE_GIC_HOLDING,
      "Duplicate gic holding with currency %s, term %s and interest rate %s found in request",
      "The request contains two or more gic holdings with the same currency, term and interest rate",
      "Provide at most one gic holding per currency, term and interest rate combination",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  HOLDING_TYPE_NOT_LEAF(
      Codes.HOLDING_TYPE_NOT_LEAF,
      "The holding %s has unsupported holding type %s; pick a specific subtype",
      "Holding type must be a leaf instrument type, not a parent category",
      "Use a leaf holding type such as MUTUAL_FUND_CANADA, ETF_CANADA, ETF_US, "
          + "MUTUAL_FUND_US, HEDGE_FUND_CANADA, SEGREGATED_FUND_CANADA, STOCK_CANADA, "
          + "STOCK_US, CASH, GIC, POOLED_FUND_CANADA, or FIXED_INCOME",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  GIC_INVESTMENT_DATE_TOO_OLD(
      Codes.GIC_INVESTMENT_DATE_TOO_OLD,
      "Investment date of holding %s must not be older than %s years",
      "Investment date of the GIC holding is older than the allowed limit",
      "Provide an investment date within the allowed range",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // MF-xxx — Mutual Fund classification errors
  // ============================================
  INVALID_SHARE_CLASS(
      Codes.INVALID_SHARE_CLASS,
      "Valid Share Class required",
      "The provided Share Class is not valid",
      "Use a valid Share Class value",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  INVALID_CATEGORY(
      Codes.INVALID_CATEGORY,
      "Valid Category required",
      "The provided Category is not valid",
      "Use a valid Category value",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // GIC-xxx — GIC holding errors
  // ============================================
  GIC_HOLDING_MISSING_INTEREST_RATE(
      Codes.GIC_HOLDING_MISSING_INTEREST_RATE,
      "The gic holding is missing interest rate",
      "Interest rate is required for GIC holdings",
      "Populate the interest rate for this GIC holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  GIC_HOLDING_MISSING_TERM(
      Codes.GIC_HOLDING_MISSING_TERM,
      "The gic holding is missing term",
      "Term is required for GIC holdings",
      "Populate the term for this GIC holding",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // IDX-xxx — Price index errors
  // ============================================
  MISSING_PRICE_INDICES(
      Codes.MISSING_PRICE_INDICES,
      "There are no price indices for index.",
      "No price indices are available for the requested index",
      "Ensure price indices are populated for the requested index",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  // ============================================
  // SYS-xxx — System errors
  // ============================================
  INVALID_DATA_PROVIDER(
      Codes.INVALID_DATA_PROVIDER,
      "Invalid or missing data provider",
      "The requested data provider is unknown or not configured",
      "Check the data provider configuration and the request value",
      HttpStatus.INTERNAL_SERVER_ERROR,
      Severity.ERROR),

  INTERNAL_SERVER_ERROR(
      Codes.INTERNAL_SERVER_ERROR,
      "Internal server error",
      "The server encountered an unexpected error",
      "Please try again later or contact support if the problem persists",
      HttpStatus.INTERNAL_SERVER_ERROR,
      Severity.ERROR),

  EXTERNAL_SERVICE_UNAVAILABLE(
      Codes.EXTERNAL_SERVICE_UNAVAILABLE,
      "External service is currently unavailable or returns a server error: %s",
      "A downstream service the calculation engine depends on is unreachable or returned a server error",
      "Retry the request later; if the problem persists, check the external service health",
      HttpStatus.SERVICE_UNAVAILABLE,
      Severity.ERROR),

  EXTERNAL_SERVICE_BAD_RESPONSE(
      Codes.EXTERNAL_SERVICE_BAD_RESPONSE,
      "External service rejected the request: %s",
      "A downstream service returned a client-error (4xx) response indicating the outbound request was malformed or unauthorized",
      "Verify the input data and credentials used for the external service call",
      HttpStatus.BAD_GATEWAY,
      Severity.ERROR),

  // ============================================
  // VAL-xxx — Generic field validation errors
  // ============================================
  BAD_INPUT(
      Codes.BAD_INPUT,
      "Bad input data",
      "Either any parameter or request body is missing or invalid",
      "Validate the request body and all required parameters",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  FIELD_NOT_NULL(
      Codes.FIELD_NOT_NULL,
      "%s must not be null",
      "A required field is null",
      "Provide a value for the indicated field",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  FIELD_NOT_BLANK(
      Codes.FIELD_NOT_BLANK,
      "%s must not be blank",
      "A required field is blank",
      "Provide a non-blank value for the indicated field",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR),

  FIELD_NOT_EMPTY(
      Codes.FIELD_NOT_EMPTY,
      "%s must not be empty",
      "A required field is empty",
      "Provide a non-empty value for the indicated field",
      HttpStatus.BAD_REQUEST,
      Severity.ERROR);

  private static final Map<String, ErrorCode> BY_CODE = Arrays.stream(values())
      .collect(java.util.stream.Collectors.toUnmodifiableMap(ErrorCode::getCode, e -> e));

  private final String code;
  private final String message;
  private final String description;
  private final String action;
  private final HttpStatus httpStatus;
  private final Severity severity;

  public String getFormattedMessage(Object... args) {
    if (args == null || args.length == 0) {
      return message;
    }
    if (!message.contains("%")) {
      return message + ": " + java.util.Arrays.stream(args)
          .map(String::valueOf)
          .collect(java.util.stream.Collectors.joining(", "));
    }
    return String.format(message, args);
  }

  public static ErrorCode fromCode(String code) {
    ErrorCode resolved = BY_CODE.get(code);
    if (resolved == null) {
      throw INTERNAL_SERVER_ERROR.toException(code);
    }
    return resolved;
  }

  public CalculationException toException(Object... formatArgs) {
    requireErrorSeverity();
    return new CalculationException(this, formatArgs);
  }

  public CalculationException toExceptionForHolding(PortfolioHolding holding, Object... formatArgs) {
    requireErrorSeverity();
    return new CalculationException(this, formatArgs).withHolding(holding);
  }

  public CalculationException toExceptionForId(String id, Object... formatArgs) {
    requireErrorSeverity();
    return new CalculationException(this, formatArgs).withId(id);
  }

  public CalculationException toExceptionForField(String fieldName, Object... formatArgs) {
    requireErrorSeverity();
    return new CalculationException(this, formatArgs).withFieldName(fieldName);
  }

  public ValidationException toValidationException(Object... formatArgs) {
    requireErrorSeverity();
    return new ValidationException(this, formatArgs);
  }

  public ValidationException toValidationExceptionForHolding(PortfolioHolding holding, Object... formatArgs) {
    requireErrorSeverity();
    return new ValidationException(this, formatArgs).withHolding(holding);
  }

  public ValidationException toValidationExceptionForId(String id, Object... formatArgs) {
    requireErrorSeverity();
    return new ValidationException(this, formatArgs).withId(id);
  }

  public ValidationException toValidationExceptionForField(String fieldName, Object... formatArgs) {
    requireErrorSeverity();
    return new ValidationException(this, formatArgs).withFieldName(fieldName);
  }

  private void requireErrorSeverity() {
    if (severity != Severity.ERROR) {
      throw new IllegalStateException("Cannot create an exception for ErrorCode " + name() + " with severity "
          + severity + "; only " + Severity.ERROR + " severity may be thrown as an exception. "
          + "Use one of the toNotification*/asNotification methods instead.");
    }
  }

  public Notification asNotification(Object... formatArgs) {
    return buildNotification(null, null, getFormattedMessage(formatArgs), paramMetadata(formatArgs));
  }

  public Notification toNotificationForHolding(PortfolioHolding holding, Object... formatArgs) {
    String id = holdingId(holding);
    Object[] allArgs = prepend(id, formatArgs);
    Map<String, Object> metadata = paramMetadata(allArgs);
    if (id != null) {
      metadata.put(HOLDING_ID, id);
    }
    return buildNotification(id, null, getFormattedMessage(allArgs), metadata);
  }

  public Notification toNotificationForField(String fieldName, Object... formatArgs) {
    return buildNotification(null, fieldName, getFormattedMessage(formatArgs), paramMetadata(formatArgs));
  }

  public Notification toNotification(String id, String fieldName, String formattedMessage,
      Map<String, Object> metadata) {
    return buildNotification(id, fieldName, formattedMessage == null ? message : formattedMessage, metadata);
  }

  public Notification toNotification() {
    return buildNotification(null, null, message, null);
  }

  private Notification buildNotification(String id, String fieldName, String formattedMessage,
      Map<String, Object> metadata) {
    return Notification.builder()
        .uuid(id == null ? UUID.randomUUID().toString() : id)
        .code(code)
        .message(formattedMessage)
        .description(description)
        .action(action)
        .severity(severity)
        .fieldName(fieldName)
        .metadata(metadata == null ? new LinkedHashMap<>() : metadata)
        .build();
  }

  /**
   * Compile-time string literals of every {@link ErrorCode} constant's code. Needed because Jakarta validation
   * annotation attributes only accept compile-time constants.
   */
  @UtilityClass
  public static final class Codes {
    // FDS
    public static final String MISSING_SECTOR_NAME = "FDS-001";
    public static final String MISSING_STYLE_BOX = "FDS-002";
    public static final String UNKNOWN_TYPE_FROM_DATA_POINT = "FDS-003";
    public static final String MISSING_EQUITY_SECTOR_ALLOCATION = "FDS-004";
    public static final String MISSING_EQUITY_STYLEBOX_EXPOSURE = "FDS-005";
    public static final String MISSING_FIXED_INCOME_STYLEBOX_EXPOSURE = "FDS-006";
    public static final String MISSING_CLASSIFICATION_ALLOCATION = "FDS-007";
    public static final String MISSING_MATURITY_ALLOCATION = "FDS-008";
    public static final String MISSING_INCOME_FORECAST_DIVIDEND_YIELD = "FDS-009";
    public static final String MISSING_YIELD_DIVIDEND_YIELD = "FDS-010";
    public static final String MISSING_INCOME_FORECAST_PAYOUT_SCHEDULE = "FDS-011";
    public static final String MISSING_INCOME_FORECAST_PAYMENT_FREQUENCY = "FDS-012";
    public static final String MISSING_INCOME_FORECAST_MATURITY_DATE = "FDS-013";
    public static final String MISSING_INCOME_FORECAST_ISSUE_DATE = "FDS-014";
    public static final String MISSING_EQUITY_COUNTRY_EXPOSURE = "FDS-015";
    public static final String MISSING_EQUITY_GEOGRAPHIC_EXPOSURE = "FDS-016";
    public static final String MISSING_EQUITY_MARKET_CAPITALIZATION = "FDS-017";
    public static final String MISSING_ASSET_ALLOCATION = "FDS-018";
    public static final String MISSING_BOND_COUNTRY_EXPOSURE = "FDS-019";
    public static final String MISSING_CREDIT_QUALITY = "FDS-020";
    public static final String MISSING_FIXED_INCOME_BOND_SECTOR = "FDS-021";
    public static final String MISSING_MANAGEMENT_EXPENSE_RATIO = "FDS-022";
    public static final String MISSING_ACTUAL_MANAGEMENT_FEE = "FDS-023";
    public static final String MISSING_NET_EXPENSE_RATIO = "FDS-024";
    public static final String MISSING_GROSS_EXPENSE_RATIO = "FDS-025";
    public static final String MISSING_BUSINESS_COUNTRY_CODE = "FDS-026";
    public static final String MISSING_COMPANY_NAME = "FDS-027";
    public static final String UNDERLYING_FUND_MISSING_UNDERLYING_HOLDINGS = "FDS-028";
    public static final String MISSING_HOLDING_IDENTIFIERS = "FDS-029";
    public static final String MISSING_UNDERLYING_HOLDING_ID_VALUE = "FDS-030";
    public static final String MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE = "FDS-031";

    // FX
    public static final String FX_RATES_UNAVAILABLE = "FX-001";

    // T-Bill (risk-free rate)
    public static final String MISSING_TBILL_RATE = "TBL-001";
    public static final String TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY = "TBL-002";

    // Security catalog (existence)
    public static final String NO_SECURITY_DATA_FOR_HOLDING = "SEC-001";
    public static final String SECURITY_NOT_FOUND_FOR_METRIC = "SEC-002";

    // Returns / NAV
    public static final String MISSING_MONTHLY_RETURNS = "RET-001";
    public static final String MISSING_HISTORICAL_NAV_PRICES = "RET-002";
    public static final String MISSING_HISTORICAL_NAV_PRICES_FOR_MONTH = "RET-003";
    public static final String MISSING_MONTHLY_RETURN_FOR_DATE = "RET-004";
    public static final String HOLDING_MISSING_LATEST_MONTHLY_RETURN = "RET-005";
    public static final String HOLDING_PSD_OUT_OF_RANGE = "RET-006";
    public static final String NAV_PARAM_MISSING = "RET-007";
    public static final String INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD = "RET-008";
    public static final String CIPSD_OUTSIDE_DATA_RANGE = "RET-009";
    public static final String NO_COMPLETE_CALENDAR_YEAR = "RET-010";
    public static final String INCOMPLETE_YEAR_SKIPPED = "RET-011";
    public static final String DEGENERATE_GROWTH_DATA = "RET-012";
    public static final String MISSING_PORTFOLIO_RETURN_FOR_DATE = "RET-013";
    public static final String MISSING_BENCHMARK_RETURN_FOR_DATE = "RET-014";
    public static final String PERIOD_RESULT_NOT_AVAILABLE = "RET-015";

    // Performance Dates
    public static final String CPSD_AFTER_CPED = "PFD-001";
    public static final String CPSD_BEFORE_PORTFOLIO_PSD = "PFD-002";
    public static final String CPSD_AFTER_PORTFOLIO_PED = "PFD-003";
    public static final String CPSD_BEFORE_BENCHMARK_PSD = "PFD-004";
    public static final String CPSD_AFTER_BENCHMARK_PED = "PFD-005";
    public static final String CPED_BEFORE_PORTFOLIO_PSD = "PFD-006";
    public static final String CPED_AFTER_PORTFOLIO_PED = "PFD-007";
    public static final String CPED_BEFORE_BENCHMARK_PSD = "PFD-008";
    public static final String CPED_AFTER_BENCHMARK_PED = "PFD-009";
    public static final String CPED_NOT_MONTH_END = "PFD-010";
    public static final String CIPSD_NOT_MONTH_END = "PFD-011";
    public static final String CIPSD_AFTER_CPED = "PFD-012";
    public static final String CPSD_NOT_MONTH_END = "PFD-013";

    // Time Interval Periods
    public static final String TIME_INTERVAL_PERIOD_LESS_THAN_12 = "TIP-001";
    public static final String TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE = "TIP-002";
    public static final String TIME_INTERVAL_PERIOD_NOT_POSITIVE = "TIP-003";
    public static final String TIME_INTERVAL_PERIOD_NOT_ALLOWED = "TIP-004";
    public static final String REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD = "TIP-005";
    public static final String REQUEST_CONTAINS_CUSTOM_PED = "TIP-006";
    public static final String TIME_INTERVAL_PERIOD_CONTAINS_SINCE_PSD = "TIP-007";
    public static final String TIME_INTERVAL_PERIOD_CONTAINS_SINCE_CIPSD = "TIP-008";
    public static final String ROLLING_INTERVAL_LESS_THAN_12 = "TIP-009";
    public static final String ROLLING_TIME_INTERVAL_NOT_POSITIVE = "TIP-010";

    // Calculation metric request
    public static final String UNSUPPORTED_METRIC = "MET-001";
    public static final String METRIC_MISMATCH = "MET-002";
    public static final String DUPLICATE_METRIC = "MET-003";
    public static final String METRIC_REQUIRED = "MET-004";

    // Currency
    public static final String PORTFOLIO_MISSING_CURRENCY = "CUR-001";
    public static final String HOLDING_MISSING_CURRENCY = "CUR-002";
    public static final String HOLDING_MISSING_CURRENCY_FROM_FDS = "CUR-003";
    public static final String UNSUPPORTED_CURRENCY_FROM_FDS = "CUR-004";

    // MER / Fees
    public static final String MISSING_MER_AND_MANAGEMENT_FEE = "MER-001";
    public static final String MISSING_NER_AND_GER = "MER-002";
    public static final String MISSING_MANAGEMENT_FEE = "MER-003";
    public static final String MISSING_SALES_CHARGE_TYPE = "MER-004";
    public static final String MISSING_FUND_FEE_DATA = "MER-005";

    // Best / Worst Periods
    public static final String BEST_WORST_TIME_INTERVAL_NOT_POSITIVE = "BWP-001";
    public static final String BEST_WORST_TIME_INTERVAL_TOO_LARGE = "BWP-002";

    // Top Common Holdings
    public static final String HOLDING_MISSING_UNDERLYING_HOLDINGS = "TCH-001";
    public static final String ACCUMULATE_HOLDING_TYPES_EXCEED_MAX = "TCH-003";
    public static final String GIC_HOLDING_NAME_EMPTY = "TCH-005";
    public static final String HOLDING_MISSING_WEIGHTING_FROM_FDS = "TCH-006";

    // Distribution of Returns
    public static final String CUSTOM_NUMBER_OF_BINS_LESS_THAN_MIN = "DIS-001";
    public static final String CUSTOM_NUMBER_OF_BINS_GREATER_THAN_MAX = "DIS-002";

    // Holdings validation
    public static final String HOLDING_VALUE_NEGATIVE_OR_NULL = "HLD-001";
    public static final String DUPLICATE_HOLDING = "HLD-002";
    public static final String HOLDING_VALUES_SUM_NOT_POSITIVE = "HLD-003";
    public static final String HOLDING_TYPE_NOT_LEAF = "HLD-004";
    public static final String DUPLICATE_CASH_HOLDING = "HLD-005";
    public static final String DUPLICATE_GIC_HOLDING = "HLD-006";

    // Mutual Fund classification
    public static final String INVALID_SHARE_CLASS = "MF-001";
    public static final String INVALID_CATEGORY = "MF-002";

    // GIC
    public static final String GIC_HOLDING_MISSING_INTEREST_RATE = "GIC-001";
    public static final String GIC_HOLDING_MISSING_TERM = "GIC-002";
    public static final String GIC_INVESTMENT_DATE_TOO_OLD = "GIC-003";

    // Price Index
    public static final String MISSING_PRICE_INDICES = "IDX-001";

    // System
    public static final String INVALID_DATA_PROVIDER = "SYS-001";
    public static final String INTERNAL_SERVER_ERROR = "SYS-002";
    public static final String EXTERNAL_SERVICE_UNAVAILABLE = "SYS-003";
    public static final String EXTERNAL_SERVICE_BAD_RESPONSE = "SYS-004";

    // Generic validation
    public static final String BAD_INPUT = "VAL-004";
    public static final String FIELD_NOT_NULL = "VAL-001";
    public static final String FIELD_NOT_BLANK = "VAL-002";
    public static final String FIELD_NOT_EMPTY = "VAL-003";
  }
}
