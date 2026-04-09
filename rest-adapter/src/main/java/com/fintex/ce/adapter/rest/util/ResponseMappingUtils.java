package com.fintex.ce.adapter.rest.util;

import com.fintex.ce.adapter.rest.dto.response.AlphaResDTO;
import com.fintex.ce.adapter.rest.dto.response.AnnualReturnResDTO;
import com.fintex.ce.adapter.rest.dto.response.AssetAllocationEMResDTO;
import com.fintex.ce.adapter.rest.dto.response.AssetAllocationResDTO;
import com.fintex.ce.adapter.rest.dto.response.AverageMerResponse;
import com.fintex.ce.adapter.rest.dto.response.BestWorstPeriodsResponseDTO;
import com.fintex.ce.adapter.rest.dto.response.BetaResDTO;
import com.fintex.ce.adapter.rest.dto.response.ClassificationAllocationResDto;
import com.fintex.ce.adapter.rest.dto.response.CommonPerformanceDatesResDTO;
import com.fintex.ce.adapter.rest.dto.response.CorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.response.CountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.CreditQualityResDTO;
import com.fintex.ce.adapter.rest.dto.response.DownsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.response.DownsideDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityCountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityMarketCapResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquitySectorResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.response.ExcessReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.FixedIncomeSectorResDTO;
import com.fintex.ce.adapter.rest.dto.response.FixedIncomeStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.response.GeographicExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.Growth10KResDTO;
import com.fintex.ce.adapter.rest.dto.response.IncomeForecastResDto;
import com.fintex.ce.adapter.rest.dto.response.InformationRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.LeadingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.MARRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.ManagementFeeResponse;
import com.fintex.ce.adapter.rest.dto.response.MaturityAllocationResDto;
import com.fintex.ce.adapter.rest.dto.response.MaxDrawdownResDTO;
import com.fintex.ce.adapter.rest.dto.response.MeanResDTO;
import com.fintex.ce.adapter.rest.dto.response.RSquaredResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingCorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingSharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingStandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.SalesChargeResDtos;
import com.fintex.ce.adapter.rest.dto.response.SharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.SortinoRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.StandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.TopCommonHoldingsResDTO;
import com.fintex.ce.adapter.rest.dto.response.TrackingErrorResDTO;
import com.fintex.ce.adapter.rest.dto.response.TrailingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.TreynorRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.UpsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.response.YieldResDto;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.adapter.rest.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import java.util.Map;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ResponseMappingUtils {

  private static final Map<CalculationMetric, Supplier<? extends ErrorDTO>> RESPONSE_FACTORIES = Map.ofEntries(
      Map.entry(CalculationMetric.TRAILING_TOTAL_RETURNS, TrailingTotalReturnsResDTO::new),
      Map.entry(CalculationMetric.LEADING_TOTAL_RETURNS, LeadingTotalReturnsResDTO::new),
      Map.entry(CalculationMetric.ROLLING_TOTAL_RETURNS, RollingTotalReturnsResDTO::new),
      Map.entry(CalculationMetric.EXCESS_RETURNS, ExcessReturnsResDTO::new),
      Map.entry(CalculationMetric.ANNUAL_RETURNS, AnnualReturnResDTO::new),
      Map.entry(CalculationMetric.GROWTH_OF_10K, Growth10KResDTO::new),
      Map.entry(CalculationMetric.BEST_WORST_PERIODS, BestWorstPeriodsResponseDTO::new),
      Map.entry(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS, DistributionOfReturnsResDTO::new),
      Map.entry(CalculationMetric.STANDARD_DEVIATION, StandardDeviationResDTO::new),
      Map.entry(CalculationMetric.ROLLING_STANDARD_DEVIATION, RollingStandardDeviationResDTO::new),
      Map.entry(CalculationMetric.MEAN, MeanResDTO::new),
      Map.entry(CalculationMetric.SHARPE_RATIO, SharpeRatioResDTO::new),
      Map.entry(CalculationMetric.ROLLING_SHARPE_RATIO, RollingSharpeRatioResDTO::new),
      Map.entry(CalculationMetric.SORTINO_RATIO, SortinoRatioResDTO::new),
      Map.entry(CalculationMetric.MAX_DRAWDOWN, MaxDrawdownResDTO::new),
      Map.entry(CalculationMetric.DOWNSIDE_DEVIATION, DownsideDeviationResDTO::new),
      Map.entry(CalculationMetric.MAR_RATIO, MARRatioResDTO::new),
      Map.entry(CalculationMetric.TREYNOR_RATIO, TreynorRatioResDTO::new),
      Map.entry(CalculationMetric.INFORMATION_RATIO, InformationRatioResDTO::new),
      Map.entry(CalculationMetric.TRACKING_ERROR, TrackingErrorResDTO::new),
      Map.entry(CalculationMetric.ALPHA, AlphaResDTO::new),
      Map.entry(CalculationMetric.BETA, BetaResDTO::new),
      Map.entry(CalculationMetric.R_SQUARED, RSquaredResDTO::new),
      Map.entry(CalculationMetric.CORRELATION, CorrelationResDTO::new),
      Map.entry(CalculationMetric.ROLLING_CORRELATION, RollingCorrelationResDTO::new),
      Map.entry(CalculationMetric.UPSIDE_CAPTURE, UpsideCaptureResDTO::new),
      Map.entry(CalculationMetric.DOWNSIDE_CAPTURE, DownsideCaptureResDTO::new),
      Map.entry(CalculationMetric.ASSET_ALLOCATIONS, AssetAllocationResDTO::new),
      Map.entry(CalculationMetric.ASSET_ALLOCATIONS_EM, AssetAllocationEMResDTO::new),
      Map.entry(CalculationMetric.EQUITY_SECTOR, EquitySectorResDTO::new),
      Map.entry(CalculationMetric.EQUITY_COUNTRY_EXPOSURE, EquityCountryExposureResDTO::new),
      Map.entry(CalculationMetric.EQUITY_STYLEBOX_EXPOSURE, EquityStyleboxExposureResDto::new),
      Map.entry(CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE, GeographicExposureResDTO::new),
      Map.entry(CalculationMetric.EQUITY_MARKET_CAPITALIZATION, EquityMarketCapResDTO::new),
      Map.entry(CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE, CountryExposureResDTO::new),
      Map.entry(CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE, GeographicExposureResDTO::new),
      Map.entry(CalculationMetric.FIXED_INCOME_BOND_SECTOR, FixedIncomeSectorResDTO::new),
      Map.entry(CalculationMetric.FIXED_INCOME_STYLEBOX_EXPOSURE, FixedIncomeStyleboxExposureResDto::new),
      Map.entry(CalculationMetric.MATURITY_ALLOCATION, MaturityAllocationResDto::new),
      Map.entry(CalculationMetric.CLASSIFICATION_ALLOCATION, ClassificationAllocationResDto::new),
      Map.entry(CalculationMetric.MER, AverageMerResponse::new),
      Map.entry(CalculationMetric.MANAGEMENT_FEE, ManagementFeeResponse::new),
      Map.entry(CalculationMetric.SALES_CHARGE, SalesChargeResDtos::new),
      Map.entry(CalculationMetric.INCOME_FORECAST, IncomeForecastResDto::new),
      Map.entry(CalculationMetric.YIELD, YieldResDto::new),
      Map.entry(CalculationMetric.COMMON_PERFORMANCE_DATES, CommonPerformanceDatesResDTO::new),
      Map.entry(CalculationMetric.TOP_COMMON_HOLDINGS, TopCommonHoldingsResDTO::new),
      Map.entry(CalculationMetric.FIXED_INCOME_CREDIT_QUALITY, CreditQualityResDTO::new));

  public static Supplier<? extends ErrorDTO> getResponseFactory(CalculationMetric metric) {
    Supplier<? extends ErrorDTO> factory = RESPONSE_FACTORIES.get(metric);
    if (factory == null) {
      throw new IllegalArgumentException("No response mapping for metric: " + metric);
    }
    return factory;
  }
}
