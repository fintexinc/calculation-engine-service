package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.AnnualReturnCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.AnnualReturnResDTO;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.interfaces.calculation.AnnualReturnService;
import com.fintex.ce.util.validation.request.ReturnReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class AnnualReturnServiceImpl implements AnnualReturnService {

    private final MonthlyReturnsService monthlyReturnsService;
    private final ReturnReqDtoValidator requestValidator;

    @Autowired
    public AnnualReturnServiceImpl(final MonthlyReturnsService monthlyReturnsService,
                                   final ReturnReqDtoValidator requestValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.requestValidator = requestValidator;
    }

    @Override
    public AnnualReturnResDTO<Integer> perform(final ReturnReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
        return buildAnnualReturnCalculation(inputDTO).calculate();
    }

    AnnualReturnCalculation buildAnnualReturnCalculation(final CalculationDTO inputDTO) {
        return new AnnualReturnCalculation(inputDTO.getWeightedAveragePortfolioReturns(), inputDTO.getWarnings());
    }

    protected CalculationDTO buildWeightedAverageInputDto(final ReturnReqDTO reqDTO) {
        final Returns<RMonthlyReturns> monthlyReturns =
                monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

        final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns).setWarnings(monthlyReturns.getErrorsAsWarnings());
    }

}
