package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.EquitySectorResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.EquitySectorCacheStorage;
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
public class EquitySectorCalculationImpl extends BreakdownAbstractService<EquitySectorResDTO, EquitySectorAllocationType> {

    private final EquitySectorCacheStorage equitySectorCacheStorage;

    static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

    static {
        Stream.of(EquitySectorAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
    }

    public EquitySectorCalculationImpl(final EquitySectorCacheStorage equitySectorCacheStorage,
                                       final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.equitySectorCacheStorage = equitySectorCacheStorage;
    }

    @Override
    public EquitySectorResDTO calculate(final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> sectors,
                                        final List<Holding> holdings,
                                        final List<Warning> warnings) {
        if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
            return new EquitySectorResDTO(DEFAULT_MAP, warnings);
        }
        final Map<EquitySectorAllocationType, BigDecimal> netProducts = calculateNetProducts(sectors, holdings, EquitySectorAllocationType.values());
        final Map<EquitySectorAllocationType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
        return new EquitySectorResDTO(scaledValues, warnings);
    }

    @Override
    public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                             final List<Warning> warnings) {
        return equitySectorCacheStorage.load(reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
    }

}
