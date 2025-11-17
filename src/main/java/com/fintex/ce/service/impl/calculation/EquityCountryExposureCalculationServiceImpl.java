package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquityCountryExposureResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.EquityCountryAllocationCacheStorage;
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
public class EquityCountryExposureCalculationServiceImpl extends BreakdownAbstractService<EquityCountryExposureResDTO, CountryRegionType> {

    private final EquityCountryAllocationCacheStorage countryAllocationCacheStorage;

    static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    public EquityCountryExposureCalculationServiceImpl(final EquityCountryAllocationCacheStorage countryAllocationCacheStorage,
                                                       final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.countryAllocationCacheStorage = countryAllocationCacheStorage;
    }

    @Override
    public EquityCountryExposureResDTO calculate(final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures,
                                                 final List<Holding> holdings,
                                                 final List<Warning> warnings) {
        if (areAllValuesInMapEmpty(exposures)) {
            return new EquityCountryExposureResDTO(DEFAULT_MAP, warnings);
        }
        final Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType.values());
        final Map<CountryRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new EquityCountryExposureResDTO(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<CountryRegionType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                    final List<Warning> warnings) {
        return countryAllocationCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
