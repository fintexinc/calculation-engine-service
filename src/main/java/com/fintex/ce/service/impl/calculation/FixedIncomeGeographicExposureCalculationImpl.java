package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.GeographicExposureResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.FixedIncomeGeographicExposureCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;

@Service
public class FixedIncomeGeographicExposureCalculationImpl extends BreakdownAbstractService<GeographicExposureResDTO, GeographicRegionType> {

    private final FixedIncomeGeographicExposureCacheStorage exposureCacheStorage;

    static final Map<GeographicRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(GeographicRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    public FixedIncomeGeographicExposureCalculationImpl(FixedIncomeGeographicExposureCacheStorage exposureCacheStorage,
                                                        PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.exposureCacheStorage = exposureCacheStorage;
    }

    @Override
    public GeographicExposureResDTO calculate(Map<Holding, Map<GeographicRegionType, BigDecimal>> exposures,
                                              List<Holding> holdings,
                                              List<Warning> warnings) {
        if (areAllValuesInMapEmpty(exposures)) {
            return new GeographicExposureResDTO(DEFAULT_MAP, warnings);
        }
        Map<GeographicRegionType, BigDecimal> result = calculateNetProducts(exposures, holdings, GeographicRegionType.values());
        Map<GeographicRegionType, BigDecimal> rescaledValues = toUserScale(reScaleAbs(result));
        return new GeographicExposureResDTO(rescaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<GeographicRegionType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO,
                                                                                       List<Warning> warnings) {
        return exposureCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
