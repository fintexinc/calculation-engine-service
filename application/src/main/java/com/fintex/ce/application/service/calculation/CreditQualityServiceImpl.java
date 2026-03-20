package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.CreditQualityResponseMapper;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.service.calculation.CalculationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.model.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.model.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_CQ_CQ_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_UNKNOWN_001;
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
  private final SecurityDataFetcher<AssetAllocation> assetAllocationSecurityDataFetcher;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final CreditQualityResponseMapper responseMapper;

  public CreditQualityServiceImpl(final SecurityDataFetcher<CreditQuality> creditQualitySecurityDataFetcher,
      final SecurityDataFetcher<AssetAllocation> assetAllocationSecurityDataFetcher,
      final AssetAllocationDataMapper assetAllocationDataMapper,
      final CreditQualityResponseMapper responseMapper) {
    this.creditQualitySecurityDataFetcher = creditQualitySecurityDataFetcher;
    this.assetAllocationSecurityDataFetcher = assetAllocationSecurityDataFetcher;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
    this.responseMapper = responseMapper;
  }

  @Override
  public CreditQualityResult perform(final PortfolioHoldingsCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, CreditQuality> rawCreditQuality = creditQualitySecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = mapToRatings(rawCreditQuality, warnings);
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
    final Map<Holding, AssetAllocation> rawData = assetAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), MORNINGSTAR, EAGLE));
    final var assetAllocations = assetAllocationDataMapper.mapFromRaw(rawData, reqDTO.getHoldings());
    return assetAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, this::getFixedIncomeValue));
  }

  public BigDecimal getFixedIncomeValue(final Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry) {
    return entry.getValue().entrySet().stream()
        .filter(e2 -> AssetAllocationRegion.FIXED_INCOME.equals(e2.getKey()))
        .map(Map.Entry::getValue).findFirst().orElseThrow();
  }

  public Map<FixedIncomeCreditQuality, BigDecimal> calculate(final List<Holding> holdings,
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality) {
    final Map<CreditQualityRating, BigDecimal> ratings = calculateCreditQualityRatings(holdings, creditQuality,
        fixedIncomeCreditQuality);
    final Map<CreditQualityRating, BigDecimal> reScaled = reScaleAbs(ratings);
    return toFixedIncomeCreditQuality(reScaled);
  }

  public Map<FixedIncomeCreditQuality, BigDecimal> toFixedIncomeCreditQuality(
      final Map<CreditQualityRating, BigDecimal> reScaled) {
    final Map<FixedIncomeCreditQuality, BigDecimal> map = new EnumMap<>(FixedIncomeCreditQuality.class);
    for (FixedIncomeCreditQuality type : FixedIncomeCreditQuality.values()) {
      final BigDecimal sum = reScaled.entrySet().stream()
          .filter(e -> type.contains(e.getKey())).map(Map.Entry::getValue)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      map.put(type, sum);
    }
    return map;
  }

  public Map<CreditQualityRating, BigDecimal> calculateCreditQualityRatings(final List<Holding> holdings,
      final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality) {
    final Map<Holding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<CreditQualityRating, BigDecimal> ratingMap = new EnumMap<>(CreditQualityRating.class);
    for (CreditQualityRating rating : CreditQualityRating.values()) {
      final BigDecimal sumProduct = calculateSumProductRating(creditQuality, fixedIncomeCreditQuality, weights, rating);
      ratingMap.put(rating, divide(sumProduct, HUNDRED));
    }
    return ratingMap;
  }

  public BigDecimal calculateSumProductRating(final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality,
      final Map<Holding, BigDecimal> weights,
      final CreditQualityRating rating) {
    final Map<Holding, BigDecimal> collectedRating = creditQuality.entrySet().stream()
        .filter(e -> e.getValue().containsKey(rating))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(rating)));
    return sumProduct(collectedRating, fixedIncomeCreditQuality, weights);
  }

  private Map<Holding, Map<CreditQualityRating, BigDecimal>> mapToRatings(
      final Map<Holding, CreditQuality> rawData,
      final List<Warning> warnings) {
    return rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> mapRatings(e.getKey(), e.getValue(), warnings)));
  }

  private Map<CreditQualityRating, BigDecimal> mapRatings(final Holding holding,
      final CreditQuality creditQuality,
      final List<Warning> warnings) {
    if (CollectionUtils.isEmpty(creditQuality.getRatings())) {
      warnings.add(WRN_CQ_CQ_001.warning(holding));
      return Map.of();
    }
    final Map<CreditQualityRating, BigDecimal> map = new EnumMap<>(CreditQualityRating.class);
    creditQuality.getRatings().forEach((ratingStr, value) -> {
      final CreditQualityRating rating = CreditQualityRating.of(ratingStr);
      if (rating == null) {
        warnings.add(WRN_UNKNOWN_001.warning(holding, ratingStr, "Credit Quality"));
      } else {
        map.put(rating, value);
      }
    });
    return map;
  }

}
