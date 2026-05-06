package com.fintex.ce.application.returns.processor;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsErrorPolicy;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.model.error.ErrorCode.Codes.FX_RATES_UNAVAILABLE;

/**
 * Converts each holding's returns from its source currency into the request's target currency, falling back to the
 * original series for any holding whose required FX rates are unavailable. Failed-conversion holdings keep their
 * original currency in the snapshot's currency map; successfully converted ones are remapped to the target currency.
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

    var fxWarnings = new ArrayList<Notification>();
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> converted = fxRateService.convertReturns(
        snapshot.returnsMap(),
        snapshot.holdingCurrencyMap(),
        fxContext.rates(),
        fxContext.targetCurrency(),
        fxWarnings);

    Set<String> failedHoldingIds = fxWarnings.stream()
        .filter(notification -> FX_RATES_UNAVAILABLE.equals(notification.getCode()))
        .map(notification -> (String) notification.getMetadata().get(ErrorParams.HOLDING_ID))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<PortfolioHolding, Currency> updatedCurrencyMap = updateCurrencyMap(snapshot.holdingCurrencyMap(),
        fxContext.targetCurrency(), failedHoldingIds);

    return snapshot
        .withReturnsMap(converted)
        .withHoldingCurrencyMap(updatedCurrencyMap)
        .withAddedWarnings(fxWarnings);
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return true;
  }

  private static Map<PortfolioHolding, Currency> updateCurrencyMap(Map<PortfolioHolding, Currency> currentMap,
      Currency targetCurrency, Set<String> failedHoldingIds) {
    var updated = new HashMap<PortfolioHolding, Currency>(currentMap.size());
    currentMap.forEach((holding, currency) -> {
      if (currency == null || currency.equals(targetCurrency)) {
        updated.put(holding, currency);
        return;
      }
      updated.put(holding, failedHoldingIds.contains(holding.getIdsString()) ? currency : targetCurrency);
    });
    return updated;
  }
}
