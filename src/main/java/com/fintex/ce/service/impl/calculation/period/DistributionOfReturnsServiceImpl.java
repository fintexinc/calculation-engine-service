package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.DistributionOfReturnsCalculation;
import com.fintex.ce.domain.calculation.RollingTotalReturnsCalculation;
import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.interfaces.calculation.DistributionOfReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.DistributionOfReturnsReqValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class DistributionOfReturnsServiceImpl implements DistributionOfReturnsService {

    private final MonthlyReturnsService monthlyReturnsService;
    private final DistributionOfReturnsReqValidator requestValidator;

    public DistributionOfReturnsServiceImpl(final MonthlyReturnsService monthlyReturnsService,
                                            final DistributionOfReturnsReqValidator requestValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.requestValidator = requestValidator;
    }

    @Override
    public DistributionOfReturnsResDTO perform(final DistributionOfReturnsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final CalculationDTO inputWithScaleOfOne = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        final CalculationDTO inputWithScaleOfTwo = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of());
        final var rollingTotalReturnsCalculation = new RollingTotalReturnsCalculation(inputWithScaleOfTwo, Set.of(), trailingTotalReturnsCalculation);
        return new DistributionOfReturnsCalculation(rollingTotalReturnsCalculation, inputWithScaleOfOne.getWeightedAveragePortfolioReturns()).calculate(reqDTO);
    }

    public CalculationDTO buildCalculationDto(final DistributionOfReturnsReqDTO reqDTO, final ReturnFactorScale returnFactorScale) {
        final Returns portfolioMonthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    }
}
