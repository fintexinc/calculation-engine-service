package com.fintex.ce.application.calculation.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;
import static com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle.*;

public final class MetricBundleRegistry {

  private static final Map<CalculationMetric, Set<SecurityAttributeBundle>> REGISTRY = new EnumMap<>(
      CalculationMetric.class);

  static {
    // Period / rolling / return metrics — need monthly returns
    for (CalculationMetric m : PERIOD_METRICS) {
      register(m, MONTHLY_RETURNS);
    }
    register(ROLLING_TOTAL_RETURNS, MONTHLY_RETURNS);
    register(ROLLING_STANDARD_DEVIATION, MONTHLY_RETURNS);
    register(ROLLING_SHARPE_RATIO, MONTHLY_RETURNS);
    register(ROLLING_CORRELATION, MONTHLY_RETURNS);
    register(ANNUAL_RETURNS, MONTHLY_RETURNS);
    register(GROWTH_OF_10K, MONTHLY_RETURNS);
    register(BEST_WORST_PERIODS, MONTHLY_RETURNS);

    // Asset allocation metrics
    register(ASSET_ALLOCATIONS, ASSET_ALLOCATION, GEOGRAPHY);
    register(ASSET_ALLOCATIONS_EM, ASSET_ALLOCATION, GEOGRAPHY);

    // Equity allocation / exposure
    register(EQUITY_SECTOR, EQUITY_SECTOR_ALLOCATION);
    register(EQUITY_COUNTRY_EXPOSURE, EQUITY_COUNTRY_ALLOCATION);
    register(EQUITY_STYLEBOX_EXPOSURE, EQUITY_STYLE_BOXES);
    register(EQUITY_GEOGRAPHIC_EXPOSURE, EQUITY_GEOGRAPHIC_ALLOCATION, GEOGRAPHY);
    register(CalculationMetric.EQUITY_MARKET_CAPITALIZATION,
        SecurityAttributeBundle.EQUITY_MARKET_CAPITALIZATION);

    // Fixed income
    register(FIXED_INCOME_COUNTRY_EXPOSURE, COUNTRY_ALLOCATION);
    register(FIXED_INCOME_GEOGRAPHIC_EXPOSURE, FIXED_INCOME_GEOGRAPHIC_ALLOCATION, GEOGRAPHY);
    register(FIXED_INCOME_BOND_SECTOR, FIXED_INCOME_SECTOR_ALLOCATION);
    register(FIXED_INCOME_STYLEBOX_EXPOSURE, FIXED_INCOME_STYLE_BOXES);
    register(MATURITY_ALLOCATION, MATURITIES);
    register(CLASSIFICATION_ALLOCATION, SECURITY_CLASSIFICATION_ALLOCATION);
    register(FIXED_INCOME_CREDIT_QUALITY, CREDIT_QUALITY_RATINGS);

    // Fee / charge metrics
    register(MER, SecurityAttributeBundle.FEES);
    register(MANAGEMENT_FEE, SecurityAttributeBundle.FEES);
    register(CalculationMetric.FEES, SecurityAttributeBundle.FEES);
    register(CalculationMetric.SALES_CHARGE, SecurityAttributeBundle.SALES_CHARGE);

    // Income / yield
    register(INCOME_FORECAST, INCOME);
    register(YIELD, INCOME);

    // Holdings
    register(TOP_COMMON_HOLDINGS, TOP_HOLDINGS);
    register(NUMBER_OF_UNIQUE_HOLDINGS, HOLDING_IDENTIFIERS);
  }

  private MetricBundleRegistry() {
  }

  public static Set<SecurityAttributeBundle> bundlesFor(Collection<CalculationMetric> metrics) {
    Set<SecurityAttributeBundle> result = EnumSet.noneOf(SecurityAttributeBundle.class);
    for (CalculationMetric metric : metrics) {
      Set<SecurityAttributeBundle> bundles = REGISTRY.get(metric);
      if (bundles != null) {
        result.addAll(bundles);
      }
    }
    return result;
  }

  private static void register(CalculationMetric metric, SecurityAttributeBundle... bundles) {
    REGISTRY.computeIfAbsent(metric, k -> EnumSet.noneOf(SecurityAttributeBundle.class))
        .addAll(EnumSet.copyOf(java.util.Arrays.asList(bundles)));
  }
}
