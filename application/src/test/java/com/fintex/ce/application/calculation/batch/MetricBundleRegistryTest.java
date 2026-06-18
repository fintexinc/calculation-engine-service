package com.fintex.ce.application.calculation.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MetricBundleRegistryTest {

  @Test
  void shouldReturnEmptySet_whenMetricsListIsEmpty() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(List.of());

    assertThat(bundles).isEmpty();
  }

  @Test
  void shouldUnionBundles_whenMultipleMetricsRequireDifferentData() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(
        List.of(CalculationMetric.ASSET_ALLOCATIONS, CalculationMetric.TRAILING_TOTAL_RETURNS));

    assertThat(bundles)
        .contains(SecurityAttributeBundle.MONTHLY_RETURNS)
        .contains(SecurityAttributeBundle.ASSET_ALLOCATION)
        .contains(SecurityAttributeBundle.GEOGRAPHY);
  }

  @Test
  void shouldReturnEmptySet_whenMetricHasNoRegisteredBundles() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(
        List.of(CalculationMetric.COMMON_PERFORMANCE_DATES));

    assertThat(bundles).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"TRAILING_TOTAL_RETURNS", "EXCESS_RETURNS",
      "STANDARD_DEVIATION", "MEAN",
      "SHARPE_RATIO", "SORTINO_RATIO", "MAX_DRAWDOWN", "DOWNSIDE_DEVIATION",
      "MAR_RATIO", "TREYNOR_RATIO", "INFORMATION_RATIO", "TRACKING_ERROR",
      "ALPHA", "BETA", "R_SQUARED", "UPSIDE_CAPTURE", "DOWNSIDE_CAPTURE", "CORRELATION"})
  void shouldMapPeriodMetricsToMonthlyReturns(CalculationMetric metric) {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(List.of(metric));

    assertThat(bundles).contains(SecurityAttributeBundle.MONTHLY_RETURNS);
  }

  @Test
  void shouldMapFixedIncomeCountryExposureToCountryAllocation() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(
        List.of(CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE));

    assertThat(bundles).containsExactly(SecurityAttributeBundle.COUNTRY_ALLOCATION);
  }

  @Test
  void shouldMapAssetAllocationsToAssetAllocationAndGeography() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(
        List.of(CalculationMetric.ASSET_ALLOCATIONS));

    assertThat(bundles)
        .contains(SecurityAttributeBundle.ASSET_ALLOCATION)
        .contains(SecurityAttributeBundle.GEOGRAPHY);
  }

  @Test
  void shouldMapEquitySectorToEquitySectorAllocation() {
    assertThat(MetricBundleRegistry.bundlesFor(List.of(CalculationMetric.EQUITY_SECTOR)))
        .contains(SecurityAttributeBundle.EQUITY_SECTOR_ALLOCATION);
  }

  @Test
  void shouldMapFeeMetricsToFees() {
    Set<SecurityAttributeBundle> merBundles = MetricBundleRegistry.bundlesFor(List.of(CalculationMetric.MER));
    Set<SecurityAttributeBundle> mgmtBundles = MetricBundleRegistry.bundlesFor(List.of(
        CalculationMetric.MANAGEMENT_FEE));

    assertThat(merBundles).contains(SecurityAttributeBundle.FEES);
    assertThat(mgmtBundles).contains(SecurityAttributeBundle.FEES);
  }

  @Test
  void shouldDeduplicateBundles_whenSameMetricAppearsMultipleTimes() {
    Set<SecurityAttributeBundle> bundles = MetricBundleRegistry.bundlesFor(
        List.of(CalculationMetric.TRAILING_TOTAL_RETURNS, CalculationMetric.TRAILING_TOTAL_RETURNS));

    assertThat(bundles).containsExactly(SecurityAttributeBundle.MONTHLY_RETURNS);
  }
}
