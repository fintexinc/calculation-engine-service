package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.calculation.PeriodCalculationService;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.Period;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.util.ReturnFactorScale;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @param <E>
 *          result object
 */
public abstract class PeriodAbstractService<E extends PeriodResult, R extends PeriodCommand>
    implements
      PeriodCalculationService<R, E> {
  protected final Set<String> defaultPeriods;
  public MonthlyReturnsService monthlyReturnsService;

  protected PeriodAbstractService(MonthlyReturnsService monthlyReturnsService,
      Set<String> defaultPeriods) {
    this.monthlyReturnsService = monthlyReturnsService;
    this.defaultPeriods = defaultPeriods;
  }

  public abstract PeriodCalculationAbstract<E, ?> defineCalculationMethod(final R command);

  @Override
  public E perform(final R command) {
    final PeriodCalculationAbstract<E, ?> calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public CalculationDTO buildCalculationDto(final R command, final ReturnFactorScale returnFactorScale) {
    final ReturnsAggregate<HoldingMonthlyReturns> monthlyReturnsAggregate = monthlyReturnsService
        .getPortfolioMonthlyReturns(
            command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpedValidation(monthlyReturnsAggregate, command.getCustomPed());

    return new CalculationDTO(command.getCustomIntervalPsd(), portfolioTotalReturns);
  }

  public void addSpecificChecks(final PeriodCommand command) {
    if (CollectionUtils.isEmpty(command.getPeriods())) {
      return;
    }
    for (String period : command.getPeriods()) {
      if (StringUtils.isNumeric(period) && Integer.parseInt(period) < 12) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_LESS_THAN_12.toException();
      }
      if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE.toException();
      }
    }
  }
}
