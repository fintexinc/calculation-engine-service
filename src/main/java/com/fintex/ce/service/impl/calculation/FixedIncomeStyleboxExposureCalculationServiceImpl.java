package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.FixedIncomeStyleboxExposureResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.FixedIncomeStyleboxExposureCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Service
public class FixedIncomeStyleboxExposureCalculationServiceImpl extends BreakdownAbstractService<FixedIncomeStyleboxExposureResDto, FixedIncomeStyleboxType> {

    private final FixedIncomeStyleboxExposureCacheStorage cacheStorage;
    static final Map<FixedIncomeStyleboxType, BigDecimal> DEFAULT_MAP = new EnumMap<>(FixedIncomeStyleboxType.class);

    static {
        Stream.of(FixedIncomeStyleboxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }


    public FixedIncomeStyleboxExposureCalculationServiceImpl(final PortfolioHoldingsReqDtoValidator requestValidator,
                                                             final FixedIncomeStyleboxExposureCacheStorage cacheStorage) {
        super(requestValidator);
        this.cacheStorage = cacheStorage;
    }

    @Override
    public FixedIncomeStyleboxExposureResDto calculate(Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> exposures, List<Holding> holdings, List<Warning> warnings) {

        if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
            return new FixedIncomeStyleboxExposureResDto(DEFAULT_MAP, warnings);
        }

        final Map<FixedIncomeStyleboxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, FixedIncomeStyleboxType.values());
        final Map<FixedIncomeStyleboxType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new FixedIncomeStyleboxExposureResDto(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<FixedIncomeStyleboxType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO, List<Warning> warnings) {
        return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }
}
