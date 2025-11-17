package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.AssetAllocationEMResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.cache.EquityCountryAllocationCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.fintex.ce.config.enumeration.DataProvider.BROADRIDGE;
import static com.fintex.ce.config.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.config.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.config.enumeration.DataProvider.ENVESTNET;
import static com.fintex.ce.config.enumeration.DataProvider.MORNINGSTAR;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.ASIA_PACIFIC_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.CASH;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.EM_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.EUROPEAN_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.OTHER;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.US_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.CountryRegionType.UNITED_STATES;
import static com.fintex.ce.util.CalculationUtils.sum;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

@Service
public class AssetAllocationEMServiceImpl extends BreakdownAbstractService<AssetAllocationEMResDTO, AssetAllocationRegionEmType> {

    private final EquityCountryAllocationCacheStorage countryAllocationCacheStorage;
    private final AssetAllocationCacheStorage assetAllocationCacheStorage;
    private final AssetAllocationDataValidator assetAllocationDataValidator;
    private final AssetAllocationDataMapper assetAllocationDataMapper;
    private final DataProviderChecker dataProviderChecker;

    public AssetAllocationEMServiceImpl(final EquityCountryAllocationCacheStorage countryAllocationCacheStorage,
                                        final AssetAllocationCacheStorage assetAllocationCacheStorage,
                                        final AssetAllocationDataValidator assetAllocationDataValidator,
                                        final AssetAllocationDataMapper assetAllocationDataMapper,
                                        final DataProviderChecker dataProviderChecker,
                                        final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.countryAllocationCacheStorage = countryAllocationCacheStorage;
        this.assetAllocationCacheStorage = assetAllocationCacheStorage;
        this.assetAllocationDataValidator = assetAllocationDataValidator;
        this.assetAllocationDataMapper = assetAllocationDataMapper;
        this.dataProviderChecker = dataProviderChecker;
    }

    @Override
    public AssetAllocationEMResDTO calculate(final Map<Holding, Map<AssetAllocationRegionEmType, BigDecimal>> exposures,
                                             final List<Holding> holdings,
                                             final List<Warning> warnings) {
        final Map<AssetAllocationRegionEmType, BigDecimal> result = calculateNetProducts(exposures, holdings, AssetAllocationRegionEmType.values());
        return new AssetAllocationEMResDTO(DecimalUtils.toUserScale(result), warnings);
    }

    @Override
    public Map<Holding, Map<AssetAllocationRegionEmType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                              final List<Warning> warnings) {
        final var assetAllocationDataDto = assetAllocationCacheStorage.loadWithDataProvidesCheck(
                reqDTO.getHoldings(),
                getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS),
                warnings);
        dataProviderChecker.check(getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS), assetAllocationDataDto);
        assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
        final var assetAllocations = assetAllocationDataMapper.mapForAAEM(assetAllocationDataDto);

        return calculateAssetAllocationEMarketMap(reqDTO.getHoldings(),
                assetAllocations,
                getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS),
                warnings);
    }

    Map<Holding, Map<AssetAllocationRegion, BigDecimal>> retrieveAssetAllocations(
            final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> assetAllocations
    ) {
        return assetAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
    }

    Map<Holding, Map<AssetAllocationRegionEmType, BigDecimal>> calculateAssetAllocationEMarketMap(final List<Holding> holdings,
                                                                                                  final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> assetAllocations,
                                                                                                  final List<DataProvider> providers,
                                                                                                  final List<Warning> warnings) {
        final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap
                = countryAllocationCacheStorage.loadWithDataProvidersCheck(holdings, providers, warnings);
        final Map<Holding, BigDecimal> equityDifference = calculateEquityDifference(
                holdings, countryAllocationsMap, retrieveAssetAllocations(assetAllocations)
        );
        return holdings.stream().collect(
                toMap(
                        h -> h,
                        h -> calculateEmergingMarket(assetAllocations, countryAllocationsMap, equityDifference, h)
                )
        );
    }

    Map<AssetAllocationRegionEmType, BigDecimal> calculateEmergingMarket(final Map<Holding, Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>>> assetAllocations,
                                                                         final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap,
                                                                         final Map<Holding, BigDecimal> equityDifference,
                                                                         final Holding holding) {
        final Map<CountryRegionType, BigDecimal> countryAllocations = countryAllocationsMap.get(holding);
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> assetAllocationsPair = assetAllocations.get(holding);
        return Stream.of(AssetAllocationRegionEmType.values())
                .collect(toMap(t -> t, t -> getEmergingMarketValue(holding, assetAllocationsPair, countryAllocations, equityDifference, t)));
    }

    BigDecimal getEmergingMarketValue(final Holding holding,
                                      final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> assetPair,
                                      final Map<CountryRegionType, BigDecimal> countryAllocations,
                                      final Map<Holding, BigDecimal> equityDifference,
                                      final AssetAllocationRegionEmType type) {
        if (assetPair.getValue().isEmpty()) {
            return ZERO;
        }
        if (AssetAllocationRegionEmType.CASH.equals(type)) {
            return assetPair.getValue().get(CASH);
        } else if (AssetAllocationRegionEmType.FIXED_INCOME.equals(type)) {
            return assetPair.getValue().get(FIXED_INCOME);
        } else if (AssetAllocationRegionEmType.CANADIAN_EQUITY.equals(type)) {
            final Supplier<BigDecimal> eSupplier = () -> assetPair.getValue().get(CANADIAN_EQUITIES);
            final Supplier<BigDecimal> mSupplier = getValueFromMap(countryAllocations, CountryRegionType.CANADA);
            return selectEmergingValueForDataProvider(assetPair.getKey(), eSupplier, mSupplier, holding);
        } else if (AssetAllocationRegionEmType.US_EQUITY.equals(type)) {
            final Supplier<BigDecimal> eSupplier = () -> assetPair.getValue().get(US_EQUITIES);
            final Supplier<BigDecimal> mSupplier = getValueFromMap(countryAllocations, UNITED_STATES);
            return selectEmergingValueForDataProvider(assetPair.getKey(), eSupplier, mSupplier, holding);
        } else if (AssetAllocationRegionEmType.INTERNATIONAL_EQUITY.equals(type)) {
            return emForInternationalEquity(holding, assetPair, countryAllocations, equityDifference);
        } else if (AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY.equals(type)) {
            final Supplier<BigDecimal> eSupplier = () -> assetPair.getValue().get(EM_EQUITIES);
            final Supplier<BigDecimal> mSupplier = () ->
                    Optional.ofNullable(countryAllocations).map(p -> p.get(CountryRegionType.EMERGING_MARKET)).orElse(ZERO);
            return selectEmergingValueForDataProvider(assetPair.getKey(), eSupplier, mSupplier, holding);
        } else if (AssetAllocationRegionEmType.UNCLASSIFIED.equals(type)) {
            // OTHER
            final Supplier<BigDecimal> supplier = () -> assetPair.getValue().get(UNCLASSIFIED);
            return selectEmergingValueForDataProvider(assetPair.getKey(), supplier, supplier, holding);
        }  else {
            // OTHER
            final Supplier<BigDecimal> supplier = () -> assetPair.getValue().get(OTHER);
            return selectEmergingValueForDataProvider(assetPair.getKey(), supplier, supplier, holding);
        }
    }

    private Supplier<BigDecimal> getValueFromMap(final Map<CountryRegionType, BigDecimal> countryAllocations, final CountryRegionType type) {
        return () ->  Optional.ofNullable(countryAllocations).map(p -> (p.get(type))).orElse(ZERO);
    }

    /**
     * Calculates emerging market for international-equity
     *
     * @param holding            holding
     * @param assetPair          asset allocations pair
     * @param countryAllocations country allocations
     * @param equityDifference   equity differences
     * @return emerging market for international-equity
     */
    BigDecimal emForInternationalEquity(final Holding holding,
                                        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> assetPair,
                                        final Map<CountryRegionType, BigDecimal> countryAllocations,
                                        final Map<Holding, BigDecimal> equityDifference) {
        final Supplier<BigDecimal> eSupplier = () -> {
            final BigDecimal euroValue = assetPair.getValue().get(EUROPEAN_EQUITIES);
            final BigDecimal asiaValue = assetPair.getValue().get(ASIA_PACIFIC_EQUITIES);
            return euroValue.add(asiaValue);
        };
        final Supplier<BigDecimal> mSupplier = () -> {
            final BigDecimal equityDiff = equityDifference.get(holding);
            final BigDecimal internationalValue = Optional.ofNullable(countryAllocations)
                    .map(p -> p.get(CountryRegionType.INTERNATIONAL_DEVELOPED)).orElse(ZERO);
            return internationalValue.add(equityDiff);
        };
        return selectEmergingValueForDataProvider(assetPair.getKey(), eSupplier, mSupplier, holding);
    }

    /**
     * Calculates value by one of the entered suppliers (eagle, morningstar)
     *
     * @param dataProvider   data provider
     * @param eagleSupplier  eagle supplier
     * @param mrStarSupplier mrStar supplier
     * @param holding        holding
     * @return value calculated by one of the suppliers
     */
    BigDecimal selectEmergingValueForDataProvider(final DataProvider dataProvider,
                                                  final Supplier<BigDecimal> eagleSupplier,
                                                  final Supplier<BigDecimal> mrStarSupplier,
                                                  final Holding holding) {
        if (EAGLE.equals(dataProvider)) {
            return Objects.requireNonNull(eagleSupplier.get());
        } else if (MORNINGSTAR.equals(dataProvider) || BROADRIDGE.equals(dataProvider)
                || ENVESTNET.equals(dataProvider) || dataProvider == null) {
            return Objects.requireNonNull(mrStarSupplier.get());
        }
        final String message = String.format("Could not recognise data provider for holding: %s", holding.generateUserIdentifier());
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * Calculates equity difference for each holding
     *
     * @param holdings           holdings
     * @param countryAllocations country allocations
     * @param assetAllocations   asset allocations
     * @return map of holdings and their equity difference
     */
    Map<Holding, BigDecimal> calculateEquityDifference(final List<Holding> holdings,
                                                       final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations,
                                                       final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations) {
        final Set<AssetAllocationRegion> equities = Set.of(CANADIAN_EQUITIES, US_EQUITIES, EUROPEAN_EQUITIES, ASIA_PACIFIC_EQUITIES, EM_EQUITIES, INTERNATIONAL_EQUITIES);
        return holdings.stream().collect(toMap(h -> h, h -> calculateEquityDiff(countryAllocations, assetAllocations, equities, h)));
    }

    /**
     * Calculates equity difference
     *
     * @param countryAllocations country allocations
     * @param assetAllocations   asset allocations
     * @param equities           set of equities to perform sum for
     * @param holding            holding
     * @return equity difference
     */
    BigDecimal calculateEquityDiff(final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations,
                                   final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations,
                                   final Set<AssetAllocationRegion> equities,
                                   final Holding holding) {
        final BigDecimal equityCountrySum = countryAllocations.containsKey(holding)
                ? sum(countryAllocations.get(holding)) : ZERO;
        final BigDecimal assetAllocationEquitySum = Objects.requireNonNull(assetAllocations.get(holding)).entrySet().stream()
                .filter(e -> equities.contains(e.getKey())).map(Map.Entry::getValue).reduce(ZERO, BigDecimal::add);
        return assetAllocationEquitySum.subtract(equityCountrySum);
    }

}
