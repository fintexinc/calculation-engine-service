package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.response.CreditQualityResponseMapper;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.enumeration.CreditQualityRatingType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.sm.model.DataProvider.MORNINGSTAR;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_CQ_CQ_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

@Service
public class CreditQualityServiceImpl implements CalculationService<CreditQualityResult, PortfolioHoldingsCommand> {

  private final SecurityDataFetcher<CreditQuality> creditQualitySecurityDataFetcher;
  private final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final CreditQualityResponseMapper responseMapper;

  public CreditQualityServiceImpl(final SecurityDataFetcher<CreditQuality> creditQualitySecurityDataFetcher,
      final SecurityDataFetcher<HoldingAssetAllocation> assetAllocationSecurityDataFetcher,
      final AssetAllocationDataMapper assetAllocationDataMapper,
      final CreditQualityResponseMapper responseMapper) {
    this.creditQualitySecurityDataFetcher = creditQualitySecurityDataFetcher;
    this.assetAllocationSecurityDataFetcher = assetAllocationSecurityDataFetcher;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_CREDIT_QUALITY;
  }

  @Override
  public CreditQualityResult perform(final PortfolioHoldingsCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, CreditQuality> rawCreditQuality = creditQualitySecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> creditQuality = extractRatings(rawCreditQuality, warnings);
    if (areAllValuesInMapEmpty(creditQuality)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<Holding, BigDecimal> fixedIncomeCreditQuality = getFixedIncomeCreditQuality(reqDTO, warnings);
    final Map<FixedIncomeCreditQuality, BigDecimal> result = calculate(reqDTO.getHoldings(), creditQuality,
        fixedIncomeCreditQuality);
    return responseMapper.fromCalculatedValues(result, warnings);
  }

  public Map<Holding, BigDecimal> getFixedIncomeCreditQuality(final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    final Map<Holding, HoldingAssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), MORNINGSTAR));
    final var assetAllocations = assetAllocationDataMapper.toRegionExposures(rawData);
    return assetAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, this::getFixedIncomeValue));
  }

  public BigDecimal getFixedIncomeValue(final Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry) {
    return entry.getValue().entrySet().stream()
        .filter(e2 -> AssetAllocationRegion.FIXED_INCOME.equals(e2.getKey()))
        .map(Map.Entry::getValue).findFirst().orElseThrow();
  }

  public Map<FixedIncomeCreditQuality, BigDecimal> calculate(final List<Holding> holdings,
      final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality) {
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

  public Map<CreditQualityRatingType, BigDecimal> calculateCreditQualityRatingTypes(final List<Holding> holdings,
      final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality) {
    final Map<Holding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<CreditQualityRatingType, BigDecimal> ratingMap = new EnumMap<>(CreditQualityRatingType.class);
    for (CreditQualityRatingType rating : CreditQualityRatingType.values()) {
      final BigDecimal sumProduct = calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, rating);
      ratingMap.put(rating, divide(sumProduct, HUNDRED));
    }
    return ratingMap;
  }

  public BigDecimal calculateSumProductRating(final Map<Holding, Map<CreditQualityRatingType, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality,
      final Map<Holding, BigDecimal> weights,
      final CreditQualityRatingType rating) {
    final Map<Holding, BigDecimal> collectedRating = creditQuality.entrySet().stream()
        .filter(e -> e.getValue().containsKey(rating))
        .filter(e -> fixedIncomeCreditQuality.containsKey(e.getKey()))
        .filter(e -> weights.containsKey(e.getKey()))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(rating)));
    return sumProduct(collectedRating, fixedIncomeCreditQuality, weights);
  }

  private Map<Holding, Map<CreditQualityRatingType, BigDecimal>> extractRatings(
      final Map<Holding, CreditQuality> rawData,
      final List<Warning> warnings) {
    return rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> extractRatings(e.getKey(), e.getValue(), warnings)));
  }

  private Map<CreditQualityRatingType, BigDecimal> extractRatings(final Holding holding,
      final CreditQuality creditQuality,
      final List<Warning> warnings) {
    if (CollectionUtils.isEmpty(creditQuality.getRatings())) {
      warnings.add(WRN_CQ_CQ_001.warning(holding));
      return Map.of();
    }
    return creditQuality.getRatings();
  }

}
