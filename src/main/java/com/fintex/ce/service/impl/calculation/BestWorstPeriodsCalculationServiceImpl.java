package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.BestWorstPeriodCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.dto.response.BestWorstPeriodsResponseDTO;
import com.fintex.ce.service.interfaces.calculation.BestWorstPeriodsCalculationService;
import com.fintex.ce.util.validation.request.BestWorstPeriodsReqValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class BestWorstPeriodsCalculationServiceImpl implements BestWorstPeriodsCalculationService {

    private final BestWorstPeriodsReqValidator requestValidator;
    private final MonthlyReturnsService monthlyReturnsService;

    @Value("#{'${default.periods.best-worst-periods}'.split(',')}")
    Set<Long> defaultPeriods;

    @Autowired
    public BestWorstPeriodsCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService,
                                                  final BestWorstPeriodsReqValidator requestValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.requestValidator = requestValidator;
    }

    @Override
    public BestWorstPeriodsResponseDTO perform(final BestWorstPeriodsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
        return buildBestWorstPeriodCalculation(reqDTO, inputDTO).calculate();
    }

    BestWorstPeriodCalculation buildBestWorstPeriodCalculation(BestWorstPeriodsReqDTO reqDTO, CalculationDTO inputDTO) {
        return new BestWorstPeriodCalculation(inputDTO.getWeightedAveragePortfolioReturns(), getPeriods(reqDTO));
    }

    protected CalculationDTO buildWeightedAverageInputDto(final BestWorstPeriodsReqDTO reqDTO) {
        final Returns monthlyReturns =
                monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

        final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns);
    }

    Set<Long> getPeriods(final BestWorstPeriodsReqDTO reqDTO) {
        return !isEmpty(reqDTO.getBestWorstTimeIntervalPeriods()) ? reqDTO.getBestWorstTimeIntervalPeriods() : defaultPeriods;
    }

}
