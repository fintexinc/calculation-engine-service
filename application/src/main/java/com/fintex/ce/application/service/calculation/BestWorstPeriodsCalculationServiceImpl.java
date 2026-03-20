package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.calculation.BestWorstPeriodCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.model.result.BestWorstPeriodsResult;
import com.fintex.ce.service.calculation.CalculationService;
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
public class BestWorstPeriodsCalculationServiceImpl
    implements
      CalculationService<BestWorstPeriodsResult, BestWorstPeriodsCommand> {

  private final MonthlyReturnsService monthlyReturnsService;

  @Value("#{'${default.periods.best-worst-periods}'.split(',')}")
  public Set<Long> defaultPeriods;

  @Autowired
  public BestWorstPeriodsCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService) {
    this.monthlyReturnsService = monthlyReturnsService;
  }

  @Override
  public BestWorstPeriodsResult perform(final BestWorstPeriodsCommand reqDTO) {
    final CalculationDTO inputDTO = buildWeightedAverageInputDto(reqDTO);
    return buildBestWorstPeriodCalculation(reqDTO, inputDTO).calculate();
  }

  public BestWorstPeriodCalculation buildBestWorstPeriodCalculation(BestWorstPeriodsCommand reqDTO,
      CalculationDTO inputDTO) {
    return new BestWorstPeriodCalculation(inputDTO.getWeightedAveragePortfolioReturns(), getPeriods(reqDTO));
  }

  public CalculationDTO buildWeightedAverageInputDto(final BestWorstPeriodsCommand reqDTO) {
    final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO
        .getCurrency(), SCALE_OF_TWO);

    final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO
            .getCustomPerformanceEndDate());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(weightedAveragePortfolioReturns);
  }

  public Set<Long> getPeriods(final BestWorstPeriodsCommand reqDTO) {
    return !isEmpty(reqDTO.getBestWorstTimeIntervalPeriods())
        ? reqDTO.getBestWorstTimeIntervalPeriods()
        : defaultPeriods;
  }

}
