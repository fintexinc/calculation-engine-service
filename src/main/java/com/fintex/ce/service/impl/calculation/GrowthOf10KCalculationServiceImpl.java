package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.Growth10KCalculation;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpedDataValidation;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.CommonDates;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.Growth10KResDTO;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.interfaces.calculation.GrowthOf10KCalculationService;
import com.fintex.ce.util.validation.request.ReturnReqDtoValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class GrowthOf10KCalculationServiceImpl implements GrowthOf10KCalculationService {

    private final MonthlyReturnsService monthlyReturnsService;
    private final ReturnReqDtoValidator requestValidator;

    @Autowired
    public GrowthOf10KCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService,
                                             final ReturnReqDtoValidator requestValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.requestValidator = requestValidator;
    }

    @Override
    public Growth10KResDTO perform(final ReturnReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        return calculateDefaultGrowthOf10K(reqDTO);
    }

    private Growth10KResDTO calculateDefaultGrowthOf10K(final ReturnReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO);
        Growth10KCalculation growth10KCalculation = buildGrowth10kCalculation(reqDTO, inputDTO);
        return growth10KCalculation.calculate();
    }

    Growth10KCalculation buildGrowth10kCalculation(ReturnReqDTO reqDTO, CalculationDTO inputDTO) {
        return new Growth10KCalculation(
                inputDTO.getWeightedAveragePortfolioReturns(),
                new CommonDates(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()),
                false,
                inputDTO.getWarnings());
    }

    protected CalculationDTO buildCalculationDto(final ReturnReqDTO reqDTO) {
        final Returns<RMonthlyReturns> monthlyReturns =
                monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

        monthlyReturns
                .setCpedDataValidation(new PortfolioCpedDataValidation())
                .setCpsdDataValidation(new PortfolioCpsdDataValidation());

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(monthlyReturns.getErrorsAsWarnings());
    }
}
