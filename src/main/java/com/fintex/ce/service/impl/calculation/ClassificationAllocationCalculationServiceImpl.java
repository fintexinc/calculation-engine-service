package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.ClassificationAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.ClassificationAllocationResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.ClassificationAllocationCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.ClassificationAllocationReqValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.CalculationUtils.reScale;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Service
public class ClassificationAllocationCalculationServiceImpl extends BreakdownAbstractService<ClassificationAllocationResDto, ClassificationAllocationType> {

    static final Map<ClassificationAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(ClassificationAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    private final ClassificationAllocationCacheStorage cacheStorage;


    public ClassificationAllocationCalculationServiceImpl(final ClassificationAllocationReqValidator requestValidator,
                                                          final ClassificationAllocationCacheStorage cacheStorage) {
        super(requestValidator);
        this.cacheStorage = cacheStorage;
    }

    @Override
    public ClassificationAllocationResDto calculate(final Map<Holding, Map<ClassificationAllocationType, BigDecimal>> exposures,
                                                    final List<Holding> holdings,
                                                    final List<Warning> warnings) {

        if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
            return new ClassificationAllocationResDto(DEFAULT_MAP, warnings);
        }

        final Map<ClassificationAllocationType, BigDecimal> netProducts = calculateNetProducts(
                exposures, holdings, ClassificationAllocationType.values());
        final Map<ClassificationAllocationType, BigDecimal> scaledValues = toUserScale(reScale(netProducts));
        return new ClassificationAllocationResDto(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<ClassificationAllocationType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                               final List<Warning> warnings) {
        return cacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
