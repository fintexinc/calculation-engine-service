package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.CreditQualityResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.CreditQualityResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.output.cache.AssetAllocationCachePort;
import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.service.calculation.CalculationService;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

@Service
public class CreditQualityServiceImpl implements CalculationService<CreditQualityResult, PortfolioHoldingsCommand> {

  private final HoldingDataLoader<Map<Holding, Map<CreditQualityRating, BigDecimal>>> creditQualityCachePort;
  private final AssetAllocationCachePort assetAllocationCachePort;
  private final AssetAllocationDataValidator assetAllocationDataValidator;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final CreditQualityResponseMapper responseMapper;

  public CreditQualityServiceImpl(final HoldingDataLoader<Map<Holding, Map<CreditQualityRating, BigDecimal>>> creditQualityCachePort,
      final AssetAllocationCachePort assetAllocationCachePort,
      final AssetAllocationDataValidator assetAllocationDataValidator,
      final AssetAllocationDataMapper assetAllocationDataMapper,
      final CreditQualityResponseMapper responseMapper) {
    this.creditQualityCachePort = creditQualityCachePort;
    this.assetAllocationCachePort = assetAllocationCachePort;
    this.assetAllocationDataValidator = assetAllocationDataValidator;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
    this.responseMapper = responseMapper;
  }

  @Override
  public CreditQualityResult perform(final PortfolioHoldingsCommand reqDTO) {
    final ArrayList<Warning> warnings = new ArrayList<>();
    final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality = creditQualityCachePort.load(reqDTO
        .getHoldings(), List.of(), warnings, new ParamHolderDTO());
    if (areAllValuesInMapEmpty(creditQuality)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<Holding, BigDecimal> fixedIncomeCreditQuality = getFixedIncomeCreditQuality(reqDTO, warnings);
    final Map<FixedIncomeCreditQuality, BigDecimal> result = calculate(reqDTO.getHoldings(), creditQuality,
        fixedIncomeCreditQuality);
    return responseMapper.fromCalculatedValues(result, warnings);
  }

  /**
   * Finds fixed income for each holding
   *
   * @param reqDTO
   *          portfolio request DTO
   * @param warnings
   *          warnings
   * @return map of holdings and their corresponding fixed incomes
   */
  public Map<Holding, BigDecimal> getFixedIncomeCreditQuality(final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    final AssetAllocationDataDTO assetAllocationDataDto = assetAllocationCachePort.load(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), MORNINGSTAR, EAGLE), warnings, new ParamHolderDTO());
    assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
    final var assetAllocations = assetAllocationDataMapper.mapForAA(assetAllocationDataDto);
    return assetAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, this::getFixedIncomeValue));
  }

  /**
   * Finds the fixed income value among the rest regions
   *
   * @param entry
   *          holding entry
   * @return fixed income
   */
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

  /**
   * Maps credit quality to fixed income credit quality
   *
   * @param reScaled
   *          already re-scaled credit quality map
   * @return fixed income credit quality map
   */
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

  /**
   * Calculates credit quality
   *
   * @param holdings
   *          holdings
   * @param creditQuality
   *          credit quality map
   * @param fixedIncomeCreditQuality
   *          fixed income credit quality
   * @return calculated credit quality map
   */
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

  /**
   * Calculates sum of the the {@param rating} across all {@param creditQuality} holdings
   *
   * @param creditQuality
   *          credit quality map
   * @param fixedIncomeCreditQuality
   *          fixed income credit quality
   * @param weights
   *          initial portfolio weights
   * @param rating
   *          credit quality rating
   * @return sum of the the same rating across all holdings
   */
  public BigDecimal calculateSumProductRating(final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
      final Map<Holding, BigDecimal> fixedIncomeCreditQuality,
      final Map<Holding, BigDecimal> weights,
      final CreditQualityRating rating) {
    final Map<Holding, BigDecimal> collectedRating = creditQuality.entrySet().stream()
        .filter(e -> e.getValue().containsKey(rating))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(rating)));
    return sumProduct(collectedRating, fixedIncomeCreditQuality, weights);
  }

}
