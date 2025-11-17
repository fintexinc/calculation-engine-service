package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.MaturityAllocationResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.MaturityAllocationCacheStorage;
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
public class MaturityAllocationCalculationServiceImpl extends BreakdownAbstractService<MaturityAllocationResDto, MaturityAllocationType> {

    private final MaturityAllocationCacheStorage cacheStorage;
    static final Map<MaturityAllocationType, BigDecimal> DEFAULT_MAP = new EnumMap<>(MaturityAllocationType.class);

    static {
        Stream.of(MaturityAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }


    public MaturityAllocationCalculationServiceImpl(final PortfolioHoldingsReqDtoValidator requestValidator,
                                                    final MaturityAllocationCacheStorage cacheStorage) {
        super(requestValidator);
        this.cacheStorage = cacheStorage;
    }

    @Override
    public MaturityAllocationResDto calculate(Map<Holding, Map<MaturityAllocationType, BigDecimal>> exposures, List<Holding> holdings, List<Warning> warnings) {

        if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
            return new MaturityAllocationResDto(DEFAULT_MAP, warnings);
        }

        final Map<MaturityAllocationType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, MaturityAllocationType.values());
        final Map<MaturityAllocationType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new MaturityAllocationResDto(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<MaturityAllocationType, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO, List<Warning> warnings) {
        return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }
}
