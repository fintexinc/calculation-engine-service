package com.fintex.ce.application.service.calculation.period.core;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.PeriodResult;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.service.calculation.PeriodCalculationService;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.enumeration.Period;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

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
      PeriodCalculationService<E, R> {
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
    final Returns<MonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpedValidation(monthlyReturns, command.getCustomPed());

    return new CalculationDTO(command.getCustomIntervalPsd(), portfolioTotalReturns);
  }

  public void addSpecificChecks(final PeriodCommand command) {
    if (CollectionUtils.isEmpty(command.getPeriods())) {
      return;
    }
    for (String period : command.getPeriods()) {
      if (StringUtils.isNumeric(period) && Integer.parseInt(period) < 12) {
        throw ExceptionCode.ERR_RRC_TIP_001.reqValidationError();
      }
      if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
        throw ExceptionCode.ERR_RRC_TIP_002.reqValidationError();
      }
    }
  }
}
