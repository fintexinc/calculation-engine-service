package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.RollingSharpeRatioCalculation;
import com.fintex.ce.application.calculation.SharpeRatioCalculation;
import com.fintex.ce.application.calculation.StandardDeviationCalculation;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.command.RollingCalculationCommand;
import com.fintex.ce.application.result.RollingSharpeRatioResult;
import com.fintex.ce.application.result.SharpeRatioResult;
import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingSharpeRatioCalculationServiceImpl
    extends
      PeriodAbstractService<RollingSharpeRatioResult, RollingCalculationCommand> {

  private final TBillsCacheStorage tBillsCacheStorage;

  public RollingSharpeRatioCalculationServiceImpl(
      final MonthlyReturnsService monthlyReturnsService,
      final TBillsCacheStorage tBillsCacheStorage,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsCacheStorage = tBillsCacheStorage;
  }

  @Override
  public RollingSharpeRatioResult perform(final RollingCalculationCommand reqDTO) {
    final var rollingStandardDeviationCalculation = defineCalculationMethod(reqDTO);
    return rollingStandardDeviationCalculation.calculate(reqDTO.getRollingPeriods());
  }

  @Override
  public RollingSharpeRatioCalculation defineCalculationMethod(final RollingCalculationCommand reqDTO) {
    final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());

    final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResult>(input, defaultPeriods);
    final var sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills,
        standardDeviationCalculation);

    return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation);
  }

  @Override
  public CalculationDTO buildCalculationDto(final RollingCalculationCommand reqDTO,
      final ReturnFactorScale returnFactorScale) {
    final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed());

    return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
  }

}
