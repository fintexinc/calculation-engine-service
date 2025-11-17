package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.CountryExposureResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.CountryExposureCacheStorage;
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
public class CountryExposureCalculationImpl extends BreakdownAbstractService<CountryExposureResDTO, CountryRegionType> {

    private final CountryExposureCacheStorage exposureCacheStorage;

    static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    public CountryExposureCalculationImpl(CountryExposureCacheStorage exposureCacheStorage,
                                          PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.exposureCacheStorage = exposureCacheStorage;
    }

    @Override
    public CountryExposureResDTO calculate(Map<Holding, Map<CountryRegionType, BigDecimal>> exposures,
                                           List<Holding> holdings,
                                           List<Warning> warnings) {
        if (areAllValuesInMapEmpty(exposures)) {
            return new CountryExposureResDTO(DEFAULT_MAP, warnings);
        }
        Map<CountryRegionType, BigDecimal> result = calculateNetProducts(exposures, holdings, CountryRegionType.values());
        Map<CountryRegionType, BigDecimal> rescaledValues = toUserScale(reScaleAbs(result));
        return new CountryExposureResDTO(rescaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<CountryRegionType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO,
                                                                                    List<Warning> warnings) {
        return exposureCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
