package com.fintex.ce.application.returns.processor;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.returns.PerformancePeriodCalculator;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsErrorPolicy;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorParams.HOLDING_ID;

/**
 * Converts each holding's returns from its source currency into the request's target currency. Per the FX contract
 * documented on {@link FxRateService#convertReturns}, missing month-end FX rates are dropped from a holding's series
 * and reported as non-fatal {@link Notification}s appended to the snapshot's warnings; the calculation continues with
 * whatever could be converted.
 *
 * <p>
 * The processor short-circuits when no target currency is set (the request is already single-currency). Before touching
 * FX, it asks {@link ReturnsErrorPolicy} to throw if any prior step left a fatal error in the snapshot — preserving the
 * legacy "abort the calculation before doing more work" behavior.
 * </p>
 */
@Component
@Order(220)
@RequiredArgsConstructor
public class FxConversionProcessor implements ReturnsProcessor {

  private final FxRateService fxRateService;

  @Override
  public <T extends ReturnsData> ReturnsSnapshot<T> process(ReturnsSnapshot<T> snapshot, ProcessingContext context) {
    var fxContext = context.fx();
    if (!fxContext.conversionRequired()) {
      return snapshot;
    }
    ReturnsErrorPolicy.throwIfFatal(snapshot);

    var warnings = new ArrayList<Notification>();
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> converted = fxRateService.convertReturns(
        snapshot.returnsMap(),
        snapshot.holdingCurrencyMap(),
        fxContext.rates(),
        fxContext.targetCurrency(),
        warnings);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> aligned = CollectionUtils.isEmpty(warnings)
        ? converted
        : PerformancePeriodCalculator.trimToTrailingContiguousCommonMonths(converted);
    if (!CollectionUtils.isEmpty(warnings) && aligned.values().stream().allMatch(Map::isEmpty)) {
      Notification representativeWarning = warnings.stream()
          .min(Comparator.comparing(notification -> notification.getMetadata().get(HOLDING_ID).toString()))
          .orElseThrow();
      throw new CalculationException(ErrorCode.FX_RATES_UNAVAILABLE, representativeWarning.getMetadata());
    }

    ReturnsSnapshot<T> convertedSnapshot = snapshot
        .withReturnsMap(aligned)
        .withHoldingCurrencyMap(remappedToTarget(snapshot.holdingCurrencyMap(), fxContext.targetCurrency()))
        .withAddedWarnings(warnings);
    return CollectionUtils.isEmpty(warnings)
        ? convertedSnapshot
        : convertedSnapshot.withPeriod(
            PerformancePeriodCalculator.findPerformanceStartDate(aligned),
            PerformancePeriodCalculator.findPerformanceEndDate(aligned));
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return true;
  }

  private static Map<PortfolioHolding, Currency> remappedToTarget(Map<PortfolioHolding, Currency> currentMap,
      Currency targetCurrency) {
    return currentMap.keySet().stream().collect(toMap(holding -> holding, holding -> targetCurrency));
  }
}