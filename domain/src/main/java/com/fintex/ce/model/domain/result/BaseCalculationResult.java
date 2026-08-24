package com.fintex.ce.model.domain.result;

import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.wm.commons.error.Notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base of every calculation result: the warnings a calculation may produce alongside its numbers. The concrete results
 * are declared here as the {@code oneOf} alternatives so that wherever a response is typed as a base result - the
 * per-metric map of a composite response above all - a client can still see, and deserialize, the shape the metric it
 * asked for actually returns.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Base response for all calculation metrics. Carries optional warnings produced during the "
    + "calculation. The concrete shape is the one the requested metric returns.", oneOf = {
        TrailingTotalReturnsResult.class, AnnualReturnResult.class, Growth10KResult.class,
        StandardDeviationResult.class, SharpeRatioResult.class, MaxDrawdownResult.class, AssetAllocationResult.class,
        AssetAllocationEMResult.class, EquitySectorResult.class, ConsolidatedSectorExposureResult.class,
        EquityCountryExposureResult.class, GeographicExposureResult.class, CountryExposureResult.class,
        FixedIncomeSectorResult.class, AverageMerResult.class, ManagementFeeResult.class, FeesResult.class,
        MerComparisonResult.class, CommonPerformanceDatesResult.class, TopCommonHoldingsResult.class,
        NumberOfUniqueHoldingsResult.class})
public abstract class BaseCalculationResult {

  @Schema(description = "List of warnings encountered during the calculation")
  @Builder.Default
  protected List<Notification> warnings = new ArrayList<>();
}
