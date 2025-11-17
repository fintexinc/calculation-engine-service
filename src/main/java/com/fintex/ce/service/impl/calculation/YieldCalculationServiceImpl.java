package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.YieldDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.YieldReqDTO;
import com.fintex.ce.dto.response.YieldResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.service.impl.cache.YieldCacheStorage;
import com.fintex.ce.service.interfaces.calculation.YieldService;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class YieldCalculationServiceImpl implements YieldService {
    private final YieldCacheStorage yieldCacheStorage;
    private final PortfolioHoldingsReqDtoValidator requestValidator;

    public YieldCalculationServiceImpl(final YieldCacheStorage yieldCacheStorage,
                                       final PortfolioHoldingsReqDtoValidator requestValidator) {
        this.yieldCacheStorage = yieldCacheStorage;
        this.requestValidator = requestValidator;
    }

    @Override
    public YieldResDto perform(final YieldReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final ArrayList<Warning> warnings = new ArrayList<>();
        final Map<Holding, RYield> yieldDto = yieldCacheStorage.load(
                reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());
        final YieldResDto yieldResDto = calculate(yieldDto);
        yieldResDto.setWarnings(warnings);
        return yieldResDto;
    }

    protected YieldResDto calculate(final Map<Holding, RYield> holdingRYieldMap) {
        final List<YieldDTO> yields = holdingRYieldMap.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .map(entry -> getYieldDto(entry.getKey(), entry.getValue()))
                .toList();

        final YieldResDto yieldResDto = new YieldResDto();
        yieldResDto.setYield(calculateWeightedAverageYield(yields));
        return yieldResDto;
    }

    protected YieldDTO getYieldDto(final Holding holding, final RYield rYield) {
        final BigDecimal dividendYield = getDividendYield(holding, rYield);
        final YieldDTO yieldDTO = new YieldDTO();
        yieldDTO.setYield(dividendYield);
        yieldDTO.setValue(holding.getValue());
        return yieldDTO;
    }

    private BigDecimal getDividendYield(final Holding holding,
                                        final RYield rYield) {
        return Optional.of(rYield)
                .filter(holdingYield -> Objects.nonNull(holdingYield.getDividendYield()) && holding.getType().equals(HoldingType.GIC))
                .map(holdingYield -> DecimalUtils.divide(holdingYield.getDividendYield(), new BigDecimal(100)))
                .orElse(rYield.getDividendYield());
    }

    protected BigDecimal calculateWeightedAverageYield(final List<YieldDTO> yieldDTOList) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (YieldDTO yieldDTO : yieldDTOList) {
            BigDecimal yieldValue = yieldDTO.getYield();
            BigDecimal weight = yieldDTO.getValue();

            if (Objects.nonNull(yieldValue) && Objects.nonNull(weight)) {
                weightedSum = weightedSum.add(yieldValue.multiply(weight));
                totalWeight = totalWeight.add(weight);
            }
        }

        return (totalWeight.compareTo(BigDecimal.ZERO) > 0) ?
                DecimalUtils.divide(weightedSum, totalWeight) : BigDecimal.ZERO;
    }
}
