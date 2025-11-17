package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.CreditQualityResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.cache.CreditQualityCacheStorage;
import com.fintex.ce.service.interfaces.calculation.CreditQualityService;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.config.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.config.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

@Service
public class CreditQualityServiceImpl implements CreditQualityService {

    static final Map<FixedIncomeCreditQuality, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(FixedIncomeCreditQuality.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    private final CreditQualityCacheStorage creditQualityCacheStorage;
    private final AssetAllocationCacheStorage assetAllocationCacheStorage;
    private final AssetAllocationDataValidator assetAllocationDataValidator;
    private final AssetAllocationDataMapper assetAllocationDataMapper;
    private final PortfolioHoldingsReqDtoValidator requestValidator;

    public CreditQualityServiceImpl(final CreditQualityCacheStorage creditQualityCacheStorage,
                                    final AssetAllocationCacheStorage assetAllocationCacheStorage,
                                    final AssetAllocationDataValidator assetAllocationDataValidator,
                                    final AssetAllocationDataMapper assetAllocationDataMapper,
                                    final PortfolioHoldingsReqDtoValidator requestValidator) {
        this.creditQualityCacheStorage = creditQualityCacheStorage;
        this.assetAllocationCacheStorage = assetAllocationCacheStorage;
        this.assetAllocationDataValidator = assetAllocationDataValidator;
        this.assetAllocationDataMapper = assetAllocationDataMapper;
        this.requestValidator = requestValidator;
    }

    @Override
    public CreditQualityResDTO perform(final PortfolioHoldingsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final ArrayList<Warning> warnings = new ArrayList<>();
        final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality
                = creditQualityCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
        if (areAllValuesInMapEmpty(creditQuality)) {
            return new CreditQualityResDTO(DEFAULT_MAP, warnings);
        }
        final Map<Holding, BigDecimal> fixedIncomeCreditQuality = getFixedIncomeCreditQuality(reqDTO, warnings);
        final Map<FixedIncomeCreditQuality, BigDecimal> result = calculate(reqDTO.getHoldings(), creditQuality, fixedIncomeCreditQuality);
        return new CreditQualityResDTO(toUserScale(result), warnings);
    }

    /**
     * Finds fixed income for each holding
     *
     * @param reqDTO   portfolio request DTO
     * @param warnings warnings
     * @return map of holdings and their corresponding fixed incomes
     */
    Map<Holding, BigDecimal> getFixedIncomeCreditQuality(final PortfolioHoldingsReqDTO reqDTO, final List<Warning> warnings) {
        final AssetAllocationDataDTO assetAllocationDataDto = assetAllocationCacheStorage.load(
                reqDTO.getHoldings(),
                getSpecifiedIfEmpty(reqDTO.getDataProviders(), MORNINGSTAR, EAGLE), warnings, new ParamHolderDTO());
        assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
        final var assetAllocations = assetAllocationDataMapper.mapForAA(assetAllocationDataDto);
        return assetAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, this::getFixedIncomeValue));
    }

    /**
     * Finds the fixed income value among the rest regions
     *
     * @param entry holding entry
     * @return fixed income
     */
    BigDecimal getFixedIncomeValue(final Map.Entry<Holding, Map<AssetAllocationRegion, BigDecimal>> entry) {
        return entry.getValue().entrySet().stream()
                .filter(e2 -> AssetAllocationRegion.FIXED_INCOME.equals(e2.getKey()))
                .map(Map.Entry::getValue).findFirst().orElseThrow();
    }

    Map<FixedIncomeCreditQuality, BigDecimal> calculate(final List<Holding> holdings,
                                                        final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
                                                        final Map<Holding, BigDecimal> fixedIncomeCreditQuality) {
        final Map<CreditQualityRating, BigDecimal> ratings = calculateCreditQualityRatings(holdings, creditQuality, fixedIncomeCreditQuality);
        final Map<CreditQualityRating, BigDecimal> reScaled = reScaleAbs(ratings);
        return toFixedIncomeCreditQuality(reScaled);
    }


    /**
     * Maps credit quality to fixed income credit quality
     *
     * @param reScaled already re-scaled credit quality map
     * @return fixed income credit quality map
     */
    Map<FixedIncomeCreditQuality, BigDecimal> toFixedIncomeCreditQuality(final Map<CreditQualityRating, BigDecimal> reScaled) {
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
     * @param holdings                 holdings
     * @param creditQuality            credit quality map
     * @param fixedIncomeCreditQuality fixed income credit quality
     * @return calculated credit quality map
     */
    Map<CreditQualityRating, BigDecimal> calculateCreditQualityRatings(final List<Holding> holdings,
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
     * @param creditQuality            credit quality map
     * @param fixedIncomeCreditQuality fixed income credit quality
     * @param weights                  initial portfolio weights
     * @param rating                   credit quality rating
     * @return sum of the the same rating across all holdings
     */
    BigDecimal calculateSumProductRating(final Map<Holding, Map<CreditQualityRating, BigDecimal>> creditQuality,
                                         final Map<Holding, BigDecimal> fixedIncomeCreditQuality,
                                         final Map<Holding, BigDecimal> weights,
                                         final CreditQualityRating rating) {
        final Map<Holding, BigDecimal> collectedRating = creditQuality.entrySet().stream()
                .filter(e -> e.getValue().containsKey(rating))
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(rating)));
        return sumProduct(collectedRating, fixedIncomeCreditQuality, weights);
    }

}
