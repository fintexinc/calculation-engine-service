package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.GeographicExposureResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.EquityGeographicAllocationCacheStorage;
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
public class EquityGeographicExposureCalculationServiceImpl extends BreakdownAbstractService<GeographicExposureResDTO, GeographicRegionType> {

    private final EquityGeographicAllocationCacheStorage geographicAllocationCacheStorage;

    static final Map<GeographicRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(GeographicRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    public EquityGeographicExposureCalculationServiceImpl(final EquityGeographicAllocationCacheStorage geographicAllocationCacheStorage,
                                                          final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.geographicAllocationCacheStorage = geographicAllocationCacheStorage;
    }

    @Override
    public GeographicExposureResDTO calculate(final Map<Holding, Map<GeographicRegionType, BigDecimal>> exposures,
                                              final List<Holding> holdings,
                                              final List<Warning> warnings) {
        if (areAllValuesInMapEmpty(exposures)) {
            return new GeographicExposureResDTO(DEFAULT_MAP, warnings);
        }
        final Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, GeographicRegionType.values());
        final Map<GeographicRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new GeographicExposureResDTO(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<GeographicRegionType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                       final List<Warning> warnings) {
        return geographicAllocationCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
