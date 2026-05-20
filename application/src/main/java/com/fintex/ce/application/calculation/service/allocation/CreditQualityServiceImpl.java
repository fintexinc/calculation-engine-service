package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.CalculationUtils.sumProduct;
import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static com.fintex.ce.model.error.ErrorCode.MISSING_CREDIT_QUALITY;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
@RequiredArgsConstructor
public class CreditQualityServiceImpl implements CalculationService<PortfolioHoldingsCommand, CreditQualityResult> {

  private final SecurityDataFetcher<CreditQuality> creditQualitySecurityDataFetcher;
  private final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher;
  private final CreditQualityResponseMapper responseMapper;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_CREDIT_QUALITY;
  }

  @Override
  public CreditQualityResult perform(final PortfolioHoldingsCommand command) {
    final ArrayList<Notification> warnings = new ArrayList<>();
    final Map<PortfolioHolding, CreditQuality> rawCreditQuality = creditQualitySecurityDataFetcher.fetch(
        command.getHoldings(), List.of());
    final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality = extractRatings(
        rawCreditQuality,
        warnings);
    if (areAllValuesInMapEmpty(creditQuality)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality = getFixedIncomeCreditQuality(command, warnings);
    final Map<FixedIncomeCreditQuality, BigDecimal> result = calculate(command.getHoldings(), creditQuality,
        fixedIncomeCreditQuality);
    return responseMapper.fromCalculatedValues(result, warnings);
  }

  public Map<PortfolioHolding, BigDecimal> getFixedIncomeCreditQuality(final PortfolioHoldingsCommand command,
      final List<Notification> warnings) {
    final Map<PortfolioHolding, HoldingAssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        command.getHoldings(),
        getSpecifiedIfEmpty(command.getDataProviders(), defaultDataProperties.getDataProviders()));
    return rawData.entrySet().stream().collect(toMap(Map.Entry::getKey, this::getFixedIncomeValue));
  }

  public BigDecimal getFixedIncomeValue(final Map.Entry<PortfolioHolding, HoldingAssetAllocation> entry) {
    Map<AssetAllocationRegionType, BigDecimal> allocations = entry.getValue().getAllocations();
    if (allocations == null) {
      return BigDecimal.ZERO;
    }
    return allocations.getOrDefault(AssetAllocationRegionType.FIXED_INCOME, BigDecimal.ZERO);
  }

  public Map<FixedIncomeCreditQuality, BigDecimal> calculate(final List<PortfolioHolding> holdings,
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality) {
    final Map<CreditQualityRatingType, BigDecimal> ratings = calculateCreditQualityRatingTypes(holdings, creditQuality,
        fixedIncomeCreditQuality);
    final Map<CreditQualityRatingType, BigDecimal> reScaled = reScaleAbs(ratings);
    return toFixedIncomeCreditQuality(reScaled);
  }

  public Map<FixedIncomeCreditQuality, BigDecimal> toFixedIncomeCreditQuality(
      final Map<CreditQualityRatingType, BigDecimal> reScaled) {
    final Map<FixedIncomeCreditQuality, BigDecimal> map = new EnumMap<>(FixedIncomeCreditQuality.class);
    for (FixedIncomeCreditQuality type : FixedIncomeCreditQuality.values()) {
      final BigDecimal sum = reScaled.entrySet().stream()
          .filter(e -> type.contains(e.getKey())).map(Map.Entry::getValue)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      map.put(type, sum);
    }
    return map;
  }

  public Map<CreditQualityRatingType, BigDecimal> calculateCreditQualityRatingTypes(
      final List<PortfolioHolding> holdings,
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality) {
    final Map<PortfolioHolding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<CreditQualityRatingType, BigDecimal> ratingMap = new EnumMap<>(CreditQualityRatingType.class);
    for (CreditQualityRatingType rating : CreditQualityRatingType.values()) {
      final BigDecimal sumProduct = calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, rating);
      ratingMap.put(rating, divide(sumProduct, HUNDRED));
    }
    return ratingMap;
  }

  public BigDecimal calculateSumProductRating(
      final Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<PortfolioHolding, BigDecimal> fixedIncomeCreditQuality,
      final Map<PortfolioHolding, BigDecimal> weights,
      final CreditQualityRatingType rating) {
    final Map<PortfolioHolding, BigDecimal> collectedRating = creditQuality.entrySet().stream()
        .filter(e -> e.getValue().containsKey(rating))
        .filter(e -> fixedIncomeCreditQuality.containsKey(e.getKey()))
        .filter(e -> weights.containsKey(e.getKey()))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(rating)));
    return sumProduct(collectedRating, fixedIncomeCreditQuality, weights);
  }

  private Map<PortfolioHolding, Map<CreditQualityRatingType, BigDecimal>> extractRatings(
      final Map<PortfolioHolding, CreditQuality> rawData,
      final List<Notification> warnings) {
    return rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> extractRatings(e.getKey(), e.getValue(), warnings)));
  }

  private Map<CreditQualityRatingType, BigDecimal> extractRatings(final PortfolioHolding holding,
      final CreditQuality creditQuality,
      final List<Notification> warnings) {
    if (CollectionUtils.isEmpty(creditQuality.getRatings())) {
      warnings.add(MISSING_CREDIT_QUALITY.toNotificationForHolding(holding));
      return Map.of();
    }
    return creditQuality.getRatings();
  }

}
