package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquityMarketCapResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.EquityMarketCapitalizationCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.config.enumeration.calculation.EquityMarketCapType.SMALL;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;

/**
 * Equity Market Capitalization Service
 */
@Service
public class EquityMarketCapCalculationServiceImpl extends BreakdownAbstractService<EquityMarketCapResDTO, EquityMarketCapType> {

    static final Map<EquityMarketCapType, Set<EquityMarketCapType>> GROUPS;

    static final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        GROUPS = Collections.unmodifiableMap(
                Map.of(
                        LARGE, Set.of(LARGE, GIANT),
                        MEDIUM, Set.of(MEDIUM),
                        SMALL, Set.of(SMALL, MICRO)
                )
        );
        GROUPS.keySet().forEach(f -> DEFAULT_MAP.put(f, null));
    }

    private final EquityMarketCapitalizationCacheStorage marketCapCacheStorage;

    @Autowired
    public EquityMarketCapCalculationServiceImpl(final EquityMarketCapitalizationCacheStorage marketCapCacheStorage,
                                                 final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.marketCapCacheStorage = marketCapCacheStorage;
    }

    @Override
    public Map<Holding, Map<EquityMarketCapType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                      final List<Warning> warnings) {
        return marketCapCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

    @Override
    public EquityMarketCapResDTO calculate(final Map<Holding, Map<EquityMarketCapType, BigDecimal>> exposures,
                                           final List<Holding> holdings,
                                           final List<Warning> warnings) {
        if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
            return new EquityMarketCapResDTO(DEFAULT_MAP, warnings);
        }
        final Map<EquityMarketCapType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, EquityMarketCapType.values());
        final Map<EquityMarketCapType, BigDecimal> reScaled = toUserScale(groupedResults(reScaleAbs(netProducts)));
        return new EquityMarketCapResDTO(reScaled, warnings);
    }

    Map<EquityMarketCapType, BigDecimal> groupedResults(final Map<EquityMarketCapType, BigDecimal> netProducts) {
        return GROUPS.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> calculateSumWithinTheSameGroup(netProducts, e)));
    }

    BigDecimal calculateSumWithinTheSameGroup(final Map<EquityMarketCapType, BigDecimal> netProducts,
                                              final Map.Entry<EquityMarketCapType, Set<EquityMarketCapType>> e) {
        return e.getValue().stream().map(type -> netProducts.getOrDefault(type, ZERO)).reduce(ZERO, BigDecimal::add);
    }

}
