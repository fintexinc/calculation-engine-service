package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquityStyleboxExposureResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.EquityStyleboxExposureCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Service
public class EquityStyleboxExposureCalculationServiceImpl extends BreakdownAbstractService<EquityStyleboxExposureResDto, EquityStyleboxType> {

    private final EquityStyleboxExposureCacheStorage cacheStorage;
    static final Map<EquityStyleboxType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(EquityStyleboxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }


    public EquityStyleboxExposureCalculationServiceImpl(final PortfolioHoldingsReqDtoValidator requestValidator,
                                                        final EquityStyleboxExposureCacheStorage cacheStorage) {
        super(requestValidator);
        this.cacheStorage = cacheStorage;
    }

    @Override
    public EquityStyleboxExposureResDto calculate(Map<Holding, Map<EquityStyleboxType, BigDecimal>> exposures, List<Holding> holdings, List<Warning> warnings) {

        if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
            return new EquityStyleboxExposureResDto(DEFAULT_MAP, warnings);
        }

        final Map<EquityStyleboxType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, EquityStyleboxType.values());
        final Map<EquityStyleboxType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new EquityStyleboxExposureResDto(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<EquityStyleboxType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO, List<Warning> warnings) {
        return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }
}
