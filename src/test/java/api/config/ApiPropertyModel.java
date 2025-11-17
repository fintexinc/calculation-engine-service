package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ApiPropertyModel {
    @JsonProperty("common-holdings")
    private SmokeTestPropertyModel commonHoldings;
    @JsonProperty("asset-allocation")
    private SmokeTestPropertyModel assetAllocation;
    @JsonProperty("trailingtotalreturn")
    private SmokeTestPropertyModel trailingTotalReturn;
    @JsonProperty("equity-sector")
    private SmokeTestPropertyModel equitySector;
    @JsonProperty("equity-market-cap")
    private SmokeTestPropertyModel equityMarketCap;
    @JsonProperty("asset-allocation-em")
    private SmokeTestPropertyModel assetAllocationEm;
    @JsonProperty("downsidecapture")
    private SmokeTestPropertyModel downsidecapture;
    @JsonProperty("upside-capture")
    private SmokeTestPropertyModel upsidecapture;
    @JsonProperty("r-squared")
    private SmokeTestPropertyModel rsquared;
    @JsonProperty("beta")
    private SmokeTestPropertyModel beta;
    @JsonProperty("alpha")
    private SmokeTestPropertyModel alpha;
    @JsonProperty("tracking-error")
    private SmokeTestPropertyModel trackingError;
    @JsonProperty("annual-return")
    private SmokeTestPropertyModel annualReturn;
    @JsonProperty("maxdrawdown")
    private SmokeTestPropertyModel maxdrawdown;
    @JsonProperty("excess-return")
    private SmokeTestPropertyModel excessReturn;
    @JsonProperty("standard-deviation")
    private SmokeTestPropertyModel standardDeviation;
    @JsonProperty("mean")
    private SmokeTestPropertyModel mean;
    @JsonProperty("downside-deviation")
    private SmokeTestPropertyModel downsideDeviation;
    @JsonProperty("sharpe-ratio")
    private SmokeTestPropertyModel sharpeRatio;
    @JsonProperty("mer")
    private SmokeTestPropertyModel mer;
    @JsonProperty("fixed-income-bond-sector")
    private SmokeTestPropertyModel fixedIncomeBondSector;
    @JsonProperty("best-worst-period")
    private SmokeTestPropertyModel bestWorstPeriod;
    @JsonProperty("fixed-country-exposure")
    private SmokeTestPropertyModel fixedIncomeCountryExposure;
    @JsonProperty("fixed-income-geographic-exposure")
    private SmokeTestPropertyModel fixedIncomeGeographicCountryExposure;
    @JsonProperty("equity-country-exposure")
    private SmokeTestPropertyModel equityCountryExposure;
    @JsonProperty("credit-quality")
    private SmokeTestPropertyModel creditQuality;
    @JsonProperty("sortino-ratio")
    private SmokeTestPropertyModel sortinoRatio;
    @JsonProperty("management-fee")
    private SmokeTestPropertyModel managementFee;
    @JsonProperty("leading-return")
    private SmokeTestPropertyModel leadingReturn;
    @JsonProperty("growthof10k")
    private SmokeTestPropertyModel growthOf10k;
    @JsonProperty("growthOf10KDaily")
    private SmokeTestPropertyModel growthOf10KDaily;
    @JsonProperty("correlation")
    private SmokeTestPropertyModel correlation;
    @JsonProperty("information-ratio")
    private SmokeTestPropertyModel informationRatio;
    @JsonProperty("common-performance-dates")
    private SmokeTestPropertyModel commonPerformanceDates;
    @JsonProperty("rolling-sharpe-ratio")
    private SmokeTestPropertyModel rollingSharpeRatio;
    @JsonProperty("rolling-standard-deviation")
    private SmokeTestPropertyModel rollingStandardDeviation;
    @JsonProperty("rolling-returns")
    private SmokeTestPropertyModel rollingReturns;
    @JsonProperty("mar-ratio")
    private SmokeTestPropertyModel marRatio;
    @JsonProperty("treynor-ratio")
    private SmokeTestPropertyModel treynorRatio;
    @JsonProperty("distribution-of-returns")
    private SmokeTestPropertyModel distributionOfReturns;
    @JsonProperty("median-mer")
    private SmokeTestPropertyModel medianMer;
    @JsonProperty("median-standard-deviation")
    private SmokeTestPropertyModel medianStandardDeviation;
    @JsonProperty("sales-charge")
    private SmokeTestPropertyModel salesCharge;
    @JsonProperty("equity-geographic-exposure")
    private SmokeTestPropertyModel equityGeographicExposure;
    @JsonProperty("daily-performance")
    private SmokeTestPropertyModel dailyPerformance;
    @JsonProperty("portfolio-distribution-calculation")
    private SmokeTestPropertyModel portfolioDistributionCalculation;
}
